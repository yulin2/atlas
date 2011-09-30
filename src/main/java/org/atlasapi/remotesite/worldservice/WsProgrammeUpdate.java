package org.atlasapi.remotesite.worldservice;

import static com.google.common.base.Charsets.ISO_8859_1;
import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.infoEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;
import static org.atlasapi.remotesite.worldservice.WsDataFile.AUDIO_ITEM;
import static org.atlasapi.remotesite.worldservice.WsDataFile.AUDIO_ITEM_PROG_LINK;
import static org.atlasapi.remotesite.worldservice.WsDataFile.PROGRAMME;
import static org.atlasapi.remotesite.worldservice.WsDataFile.SERIES;
import static org.atlasapi.remotesite.worldservice.model.WsAudioItem.wsAudioItemBuilder;
import static org.atlasapi.remotesite.worldservice.model.WsAudioItemProgLink.wsAudioItemProgLinkBuilder;
import static org.atlasapi.remotesite.worldservice.model.WsProgramme.wsProgrammeBuilder;
import static org.atlasapi.remotesite.worldservice.model.WsSeries.wsSeriesBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.NodeFactory;
import nu.xom.Nodes;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.remotesite.worldservice.model.WsAudioItem;
import org.atlasapi.remotesite.worldservice.model.WsAudioItemProgLink;
import org.atlasapi.remotesite.worldservice.model.WsProgramme;
import org.atlasapi.remotesite.worldservice.model.WsSeries;
import org.joda.time.DateTime;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.io.Closeables;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class WsProgrammeUpdate extends ScheduledTask {

    public static WsProgrammeUpdateBuilder worldServiceBuilder(WsDataStore wsDataStore, WsSeriesHandler seriesHandler, WsProgrammeHandler programmeHandler, AdapterLog log) {
        return new WsProgrammeUpdateBuilder(wsDataStore, seriesHandler, programmeHandler, log);
    }
    
    public static class WsProgrammeUpdateBuilder {

        private final WsDataStore wsDataStore;
        private final WsSeriesHandler seriesHandler;
        private final WsProgrammeHandler programmeHandler;
        private final AdapterLog log;

        public WsProgrammeUpdateBuilder(WsDataStore wsDataStore, WsSeriesHandler seriesHandler, WsProgrammeHandler programmeHandler, AdapterLog log) {
            this.wsDataStore = wsDataStore;
            this.seriesHandler = seriesHandler;
            this.programmeHandler = programmeHandler;
            this.log = log;
        }
        
        public WsProgrammeUpdate updateLatest() {
            return new WsProgrammeUpdate(wsDataStore, seriesHandler, programmeHandler, log, null);
        }
        
        public WsProgrammeUpdate updateForDate(DateTime date) {
            return new WsProgrammeUpdate(wsDataStore, seriesHandler, programmeHandler, log, date);
        }
        
    }
    
    private final WsDataStore wsDataStore;
    private final AdapterLog log;
    private final WsSeriesHandler seriesHandler;
    private final WsProgrammeHandler programmeHandler;
    private final DateTime date;

    private WsProgrammeUpdate(WsDataStore wsDataStore, WsSeriesHandler seriesHandler, WsProgrammeHandler programmeHandler, AdapterLog log, DateTime date) {
        this.wsDataStore = wsDataStore;
        this.seriesHandler = seriesHandler;
        this.programmeHandler = programmeHandler;
        this.log = log;
        this.date = date;
    }

    @Override
    protected void runTask() {
        log.record(infoEntry().withSource(getClass()).withDescription("%s data set update started", dataSetName()));
        
        Maybe<WsDataSet> latestData = date != null ? wsDataStore.dataForDay(date) : wsDataStore.latestData();
        
        if(latestData.isNothing()) {
            log.record(warnEntry().withSource(getClass()).withDescription("Couldn't get %s WS data", dataSetName()));
            return;
        }
        
        WsDataSet data = latestData.requireValue();
        
        log.record(infoEntry().withSource(getClass()).withDescription("Got WS data set for %s", data.getName()));
        
        try {
            processSeries(data.getSeries());
            
            //Creates a map from Item id to locations.
            Multimap<String, WsAudioItem> progIdAudioItem = processAudioProg(data.getAudioItem(), data.getAudioItemProgLink());
            
            if (progIdAudioItem != null) {
                processProgrammes(data.getProgramme(), progIdAudioItem);
            }
        }catch (Exception e) {
            log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("WS Updater failed"));
        }
        
        log.record(infoEntry().withSource(getClass()).withDescription("%s data set update finished", dataSetName()));
    }

    private String dataSetName() {
        return date == null ? "Latest" : date.toString("dd/MM/yyyy");
    }

    private void processProgrammes(WsDataSource programmes, final Multimap<String, WsAudioItem> progIdAudioItem) throws IOException {
        
        Builder builder = new Builder(new NodeFactory(){
            
            final Progress progress = new Progress(PROGRAMME.filename());
            
            @Override
            public Nodes finishMakingElement(Element elem) {
                if(elem.getLocalName().equals("row")) {
                    try {
                        WsProgramme prog = wsProgrammeFrom(elem);
                        programmeHandler.handle(prog, progIdAudioItem.get(prog.getProgId()));
                        progress.addProcessed();
                    } catch (Exception e) {
                        log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Exception processing WS Programme %s", elem.getFirstChildElement("ProgId").getValue()));
                        progress.addFailed();
                    }
                    reportStatus(progress.toString());
                }
                return super.finishMakingElement(elem);
            }
        });

        buildFromInput(programmes, builder);
    }

    private void processSeries(WsDataSource seriesSource) throws IOException {
        
        Builder builder = new Builder(new NodeFactory() {
            
            final Progress progress = new Progress(SERIES.filename());
            
            @Override
            public Nodes finishMakingElement(Element elem) {
                if(elem.getLocalName().equals("row")) {
                    try {
                        seriesHandler.handle(wsSeriesFromElem(elem));
                        progress.addProcessed();
                    } catch (Exception e) {
                        log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Exception processing WS Series %s", elem.getFirstChildElement("SeriesId").getValue()));
                        progress.addFailed();
                    }
                    reportStatus(progress.toString());
                }
                return super.finishMakingElement(elem);
            }
        });
        
        buildFromInput(seriesSource, builder);
    }

    private Multimap<String, WsAudioItem> processAudioProg(WsDataSource audioItem, WsDataSource audioItemProgLink) throws IOException {
        
        final Map<String, String> audioIdToProgId = processAudioItemLinks(audioItemProgLink);
        
        if(audioIdToProgId == null) {
            return null;
        }
        
        final Multimap<String, WsAudioItem> audioItemMap = LinkedListMultimap.create();
        
        final Progress audioItemProgress = new Progress(AUDIO_ITEM.filename());
        
        Builder builder = new Builder(new NodeFactory(){
            @Override
            public Nodes finishMakingElement(Element elem) {
                if(elem.getLocalName().equals("row")) {
                    try {
                        WsAudioItem audioItem = wsAudioItemFrom(elem);
                        audioItemMap.put(audioIdToProgId.get(audioItem.getAudioItemId()), audioItem);
                        audioItemProgress.addProcessed();
                    } catch (Exception e) {
                        log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Exception processing WS AudioItemProgLink.xml"));
                        audioItemProgress.addFailed();
                    }
                    reportStatus(audioItemProgress.toString());
                }
                return super.finishMakingElement(elem);
            }
        });
        
        buildFromInput(audioItem, builder);
        
        return audioItemMap;
    }

    private Map<String, String> processAudioItemLinks(WsDataSource audioItemProgLink) throws IOException {
        final Map<String, String> audioIdToProgId= Maps.newHashMap();
        
        final Progress linkProgress = new Progress(AUDIO_ITEM_PROG_LINK.filename());
        
        Builder builder = new Builder(new NodeFactory() {
            @Override
            public Nodes finishMakingElement(Element elem) {
                if(elem.getLocalName().equals("row")) {
                    try {
                        WsAudioItemProgLink audioProgLink = wsAudioItemProgLinkFrom(elem);
                        audioIdToProgId.put(audioProgLink.getAudioItemId(), audioProgLink.getProgId());
                        linkProgress.addProcessed();
                    } catch (Exception e) {
                        log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Exception processing WS AudioItemProgLink %s", elem.getFirstChildElement("AudioItemId").getValue()));
                        linkProgress.addFailed();
                    }
                    reportStatus(linkProgress.toString());
                }
                return super.finishMakingElement(elem);
            }
        });
        
        buildFromInput(audioItemProgLink, builder);

        return audioIdToProgId;
    }
    
    private void buildFromInput(WsDataSource source, Builder builder) throws IOException {
        boolean swallow = true;
        try {
            builder.build(new InputStreamReader(source.data(), ISO_8859_1));
            swallow = false;
        }catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Exception processing: %s", source.file()));
        } finally {
            Closeables.close(source, swallow);
        }
    }
    
    private WsProgramme wsProgrammeFrom(Element elem) {
        return wsProgrammeBuilder()
            .withProgId(valueOf(elem, "ProgId"))
            .withSeriesId(valueOf(elem, "SeriesId"))
            .withEpisodeTitle(valueOf(elem, "EpisodeTitle"))
            .withSynopsis(valueOf(elem, "Synopsis"))
            .withStrand(valueOf(elem, "Strand"))
            .withGenreCode(valueOf(elem, "GenreCode"))
            .withEpisodeNo(valueOf(elem, "EpisodeNo"))
            .withTotalNoEpisodes(valueOf(elem, "TotalNoEpisodes"))
            .withProgDuration(valueOf(elem, "ProgDuration"))
            .withFirstTxDate(valueOf(elem, "FirstTxDate"))
            .withIbmsProgrammeBR(valueOf(elem, "IbmsProgrammeBR"))
            .withLastAmendTimestamp(valueOf(elem, "LastAmendTimestamp"))
            .withTimestamp(valueOf(elem, "Timestamp"))
        .build();
    }

    private WsAudioItem wsAudioItemFrom(Element elem) {
        return wsAudioItemBuilder()
            .withAudioItemId(valueOf(elem, "AudioItemId"))
            .withBarcode(valueOf(elem, "Barcode"))
            .withTitle(valueOf(elem, "Title"))
            .withAudioDescription(valueOf(elem, "AudioDescription"))
            .withOtherInfo(valueOf(elem, "OtherInfo"))
            .withRecDate(valueOf(elem, "RecDate"))
            .withOrigTapeNo(valueOf(elem, "OrigTapeNo"))
            .withOrigMediaFilename(valueOf(elem, "OrigMediaFilename"))
            .withDuration(valueOf(elem, "Duration"))
            .withMonoFlag(valueOf(elem, "MonoFlag"))
            .withNoOfBands(valueOf(elem, "NoOfBands"))
            .withIbmsHouseMedia(valueOf(elem, "IbmsHouseMedia"))
            .withLinkAudioBroadcastQuality(valueOf(elem, "LinkAudioBroadcastQuality"))
            .withLinkAudioThumbnail(valueOf(elem, "LinkAudioThumbnail"))
            .withNoOfRecSheetFiles(valueOf(elem, "NoOfRecSheetFiles"))
            .withInputDatetime(valueOf(elem, "InputDatetime"))
            .withInputByUserId(valueOf(elem, "InputByUserId"))
            .withAllowDownloadFrom(valueOf(elem, "AllowDownloadFrom"))
            .withBatchId(valueOf(elem, "BatchId"))
            .withStatus(valueOf(elem, "Status"))
            .withLastAmendTimestamp(valueOf(elem, "LastAmendTimestamp"))
            .withTimestamp(valueOf(elem, "Timestamp"))
        .build();
    }
    
    private WsSeries wsSeriesFromElem(Element elem) {
        return wsSeriesBuilder()
            .withSeriesId(valueOf(elem, "SeriesId"))
            .withSeriesTitle(valueOf(elem, "SeriesTitle"))
            .withStrand(valueOf(elem, "Strand"))
            .withGenreCode(valueOf(elem, "GenreCode"))
            .withIbmsSeriesBr(valueOf(elem, "IbmsSeriesBR"))
            .withLastAmendTimestamp(valueOf(elem, "LastAmendTimestamp"))
            .withTimestamp(valueOf(elem, "Timestamp"))
        .build();
    }
    
    private WsAudioItemProgLink wsAudioItemProgLinkFrom(Element elem) {
        return wsAudioItemProgLinkBuilder()
            .withAudioItemId(valueOf(elem, "AudioItemId"))
            .withProgId(valueOf(elem, "ProgId"))
            .withProgAudioSeqNo(valueOf(elem, "ProgAudioSeqNo"))
            .withLastAmendTimestamp(valueOf(elem, "LastAmendTimestamp"))
            .withTimestamp(valueOf(elem, "Timestamp"))
        .build();
    }
    
    private String valueOf(Element parent, String childName) {
        return parent.getFirstChildElement(childName).getValue();
    }

    private static class Progress {
        
        int processed = 0;
        int failed = 0;
        private final String file;
        
        public Progress(String file) {
            this.file = file;
        }
        
        public Progress addProcessed() {
            processed++;
            return this;
        }
        
        public Progress addFailed() {
            failed++;
            return this;
        }
        
        public String toString() {
            return String.format("Processing %s. %s processed. %s failed.", file, processed, failed);
        }
    }
    
}
