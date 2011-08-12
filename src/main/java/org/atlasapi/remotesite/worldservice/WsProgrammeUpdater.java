package org.atlasapi.remotesite.worldservice;

import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;
import static org.atlasapi.remotesite.worldservice.model.WsAudioItem.wsAudioItemBuilder;
import static org.atlasapi.remotesite.worldservice.model.WsAudioItemProgLink.wsAudioItemProgLinkBuilder;
import static org.atlasapi.remotesite.worldservice.model.WsProgramme.wsProgrammeBuilder;
import static org.atlasapi.remotesite.worldservice.model.WsSeries.wsSeriesBuilder;

import java.io.IOException;
import java.io.InputStream;
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

import com.google.common.base.Charsets;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.io.Closeables;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class WsProgrammeUpdater extends ScheduledTask {

    private final WsDataStore wsDataStore;
    private final AdapterLog log;
    private final WsSeriesHandler seriesHandler;
    private final WsProgrammeHandler programmeHandler;

    public WsProgrammeUpdater(WsDataStore wsDataStore, WsSeriesHandler seriesHandler, WsProgrammeHandler programmeHandler, AdapterLog log) {
        this.wsDataStore = wsDataStore;
        this.seriesHandler = seriesHandler;
        this.programmeHandler = programmeHandler;
        this.log = log;
    }

    @Override
    protected void runTask() {
        Maybe<WsData> latestData = wsDataStore.latestData();
        
        if(latestData.isNothing()) {
            log.record(warnEntry().withDescription("Couldn't get WS data"));
            return;
        }
        
        WsData data = latestData.requireValue();
        
        try {
            processSeries(data.getSeries());
            
            Multimap<String, WsAudioItem> progIdAudioItem = processAudioProg(data.getAudioItem(), data.getAudioItemProgLink());
            
            if (progIdAudioItem != null) {
                processProgrammes(data.getProgramme(), progIdAudioItem);
            }
        }catch (Exception e) {
            log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("WS Updater failed"));
        }
    }

    private void processProgrammes(InputStream programme, final Multimap<String, WsAudioItem> progIdAudioItem) throws IOException {
        
        final Progress progress = new Progress("Programmes.xml");
        
        Builder builder = new Builder(new NodeFactory(){
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
        
        boolean swallow = true;
        try {
            builder.build(new InputStreamReader(programme, Charsets.ISO_8859_1));
            swallow = false;
        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Exception processing WS Programme.xml"));
        } finally {
            Closeables.close(programme, swallow);
        }
        
    }

    private void processSeries(InputStream series) throws IOException {

        final Progress progress = new Progress("Series.xml");
        
        Builder builder = new Builder(new NodeFactory() {
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
        
        
        boolean swallow = true;
        try {
            builder.build(new InputStreamReader(series, Charsets.ISO_8859_1));
            swallow = false;
        }catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Exception processing WS Series.xml"));
        } finally {
            Closeables.close(series, swallow);
        }
        
    }
    

    private Multimap<String, WsAudioItem> processAudioProg(InputStream audioItem, InputStream audioItemProgLink) throws IOException {
        
        final Map<String, String> audioIdToProgId= Maps.newHashMap();
        
        final Progress linkProgress = new Progress("AudioItemProgLink.xml");
        
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
        boolean swallow = true;
        try {
            builder.build(new InputStreamReader(audioItemProgLink, Charsets.ISO_8859_1));
            swallow = false;
        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Exception processing WS AudioItemProgLink.xml"));
            return null;
        } finally {
            Closeables.close(audioItemProgLink, swallow);
        }
        
        final Multimap<String, WsAudioItem> audioItemMap = LinkedListMultimap.create();
        
        final Progress audioItemProgress = new Progress("AudioItem.xml");
        
        builder = new Builder(new NodeFactory(){
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
        
        swallow = true;
        try {
            builder.build(new InputStreamReader(audioItem, Charsets.ISO_8859_1));
            swallow = false;
        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Exception processing WS AudioItem.xml"));
            return null;
        }finally {
            Closeables.close(audioItem, swallow);
        }
        
        return audioItemMap;
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
