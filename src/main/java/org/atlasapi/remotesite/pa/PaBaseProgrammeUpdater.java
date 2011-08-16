package org.atlasapi.remotesite.pa;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;

import org.atlasapi.media.entity.Channel;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.pa.bindings.ChannelData;
import org.atlasapi.remotesite.pa.bindings.ProgData;
import org.atlasapi.remotesite.pa.data.PaProgrammeDataStore;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.xml.sax.XMLReader;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.concurrency.BoundedExecutor;
import com.metabroadcast.common.health.HealthProbe;
import com.metabroadcast.common.health.ProbeResult;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.common.time.Timestamp;

public abstract class PaBaseProgrammeUpdater extends ScheduledTask implements HealthProbe {

    private static final String NOT_CURRENTLY_RUNNING = "not currently running";

    private static final Pattern FILEDATE = Pattern.compile("^.*/(\\d+)_.*$");
    
    private final AdapterLog log;

    private final PaProgDataProcessor processor;
    private final ExecutorService executor = Executors.newFixedThreadPool(25);
    private final BoundedExecutor boundedQueue = new BoundedExecutor(executor, 50);
    private final PaChannelMap channelMap = new PaChannelMap();
    private List<Channel> supportedChannels = ImmutableList.of();
    private final AtomicInteger processed = new AtomicInteger(0);
    private final AtomicReference<Maybe<String>> processingProgramme = new AtomicReference<Maybe<String>>(Maybe.<String>nothing());
    private final AtomicReference<Maybe<String>> processingChannel = new AtomicReference<Maybe<String>>(Maybe.<String>nothing());
    private final AtomicReference<Maybe<String>> processingFile = new AtomicReference<Maybe<String>>(Maybe.<String>nothing());

    private final PaProgrammeDataStore dataStore;

    private final String slug;

    public PaBaseProgrammeUpdater(PaProgDataProcessor processor, PaProgrammeDataStore dataStore, AdapterLog log, String slug) {
        this.processor = processor;
        this.dataStore = dataStore;
        this.log = log;
        this.slug = slug;
    }
    
    public void supportChannels(Iterable<Channel> channels) {
        supportedChannels = ImmutableList.copyOf(channels);
    }
    
    private boolean isSupported(Channel channel) {
        if (supportedChannels.isEmpty() || supportedChannels.contains(channel)) {
            return true;
        }
        
        return false;
    }
    
    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
    
    protected void processFiles(Iterable<File> files) {
        try {
            JAXBContext context = JAXBContext.newInstance("org.atlasapi.remotesite.pa.bindings");
            Unmarshaller unmarshaller = context.createUnmarshaller();

            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XMLReader reader = factory.newSAXParser().getXMLReader();
            reader.setContentHandler(unmarshaller.getUnmarshallerHandler());

            for (File file : files) {
                try {
                    final String filename = file.toURI().toString();
                    Matcher matcher = FILEDATE.matcher(filename);
                    
                    if (matcher.matches()) {
                        final File fileToProcess = dataStore.copyForProcessing(file);
                        
                        final DateTimeZone zone = getTimeZone(matcher.group(1));
                        processingFile.set(Maybe.just(filename));
                        log.record(new AdapterLogEntry(Severity.INFO).withSource(PaBaseProgrammeUpdater.class).withDescription("Processing file "+filename+" with timezone "+zone.toString()));
                        processed.set(0);
                        
                        unmarshaller.setListener(new Unmarshaller.Listener() {
                            public void beforeUnmarshal(Object target, Object parent) {
                            }

                            public void afterUnmarshal(Object target, Object parent) {
                                int processedCount = processed.incrementAndGet();
                                
                                if (target instanceof ProgData) {
                                    Maybe<Channel> channel = channelMap.getChannel(Integer.valueOf(((ChannelData) parent).getChannelId()));
                                    if (channel.hasValue() && isSupported(channel.requireValue())) {
                                        try {
                                            ProgData prog = (ProgData) target;
                                            processingProgramme.set(Maybe.just(prog.getSeriesId() != null ? prog.getSeriesId() : "0" + " - " + prog.getProgId() + " - " + prog.getTitle()));
                                            processingChannel.set(Maybe.just(channel.requireValue().title()));
                                            Timestamp modified = Timestamp.of(fileToProcess.lastModified());
                                            boundedQueue.submitTask(new ProcessProgrammeJob(prog, channel.requireValue(), zone, modified));
                                        } catch (Exception e) {
                                            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaBaseProgrammeUpdater.class));
                                        }
                                    }
                                }
                                
                                reportStatus(String.format("%s: %s processed", filename, processedCount));
                            }
                        });
//                        reader.parse(new InputSource(new FileInputStream(fileToProcess)));
                        reader.parse(fileToProcess.toURI().toString());
                    }
                } catch (Exception e) {
                    log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaBaseProgrammeUpdater.class).withDescription("Error processing file " + file.toString()));
                }
            }
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaBaseProgrammeUpdater.class));
        } finally {
            processed.set(0);
            processingFile.set(Maybe.<String>nothing());
            processingProgramme.set(Maybe.<String>nothing());
            processingChannel.set(Maybe.<String>nothing());
        }
    }
    
    protected static DateTimeZone getTimeZone(String date) {
        String timezoneDateString = date + "-11:00";
        DateTime timezoneDateTime = DateTimeFormat.forPattern("yyyyMMdd-HH:mm").withZone(DateTimeZones.LONDON).parseDateTime(timezoneDateString);
        DateTimeZone zone = timezoneDateTime.getZone();
        return DateTimeZone.forOffsetMillis(zone.getOffset(timezoneDateTime));
    }
    
    private final Set<String> currentlyProcessing = Sets.newHashSet();
        
    class ProcessProgrammeJob implements Runnable {
        
        private final ProgData progData;
        private final Channel channel;
        private final DateTimeZone zone;
        private final Timestamp updatedAt;

        public ProcessProgrammeJob(ProgData progData, Channel channel, DateTimeZone zone, Timestamp updatedAt) {
            this.progData = progData;
            this.channel = channel;
            this.zone = zone;
            this.updatedAt = updatedAt;
        }

        @Override
        public void run() {
            try {
                synchronized (currentlyProcessing) {
                    while(currentlyProcessing.contains(progData.getSeriesId()) || currentlyProcessing.contains(progData.getProgId())) {
                        currentlyProcessing.wait();
                    }
                    currentlyProcessing.add(Strings.isNullOrEmpty(progData.getSeriesId()) ? progData.getProgId() : progData.getSeriesId());
                }
                if (progData != null) {
                    processor.process(progData, channel, zone, updatedAt);
                }
                synchronized (currentlyProcessing) {
                    currentlyProcessing.remove(Strings.isNullOrEmpty(progData.getSeriesId()) ? progData.getProgId() : progData.getSeriesId());
                    currentlyProcessing.notifyAll();
                }
            } catch (Exception e) {
                log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaBaseProgrammeUpdater.class).withDescription("Error processing programme " + progData));
            }
        }
    }
    
    @Override
    public String title() {
        return "Pa Updater for "+processingFile.get().valueOrDefault(NOT_CURRENTLY_RUNNING);
    }

    @Override
    public ProbeResult probe() throws Exception {
        ProbeResult probe = new ProbeResult(title());
        probe.addInfo("Currently processing programme", processingProgramme.get().valueOrDefault(NOT_CURRENTLY_RUNNING));
        probe.addInfo("Currently processing channel", processingChannel.get().valueOrDefault(NOT_CURRENTLY_RUNNING));
        probe.addInfo("Number processed so far", String.valueOf(processed.get()));
        return probe;
    }

    @Override
    public String slug() {
        return "paupdater_"+slug;
    }
}
