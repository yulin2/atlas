package org.atlasapi.remotesite.pa;

import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.infoEntry;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;

import org.atlasapi.media.entity.Channel;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.pa.PaChannelProcessJob.PaChannelProcessJobBuilder;
import org.atlasapi.remotesite.pa.bindings.ChannelData;
import org.atlasapi.remotesite.pa.bindings.ProgData;
import org.atlasapi.remotesite.pa.data.PaProgrammeDataStore;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xml.sax.XMLReader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.concurrency.BoundedExecutor;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.common.time.Timestamp;

public abstract class PaBaseProgrammeUpdater extends ScheduledTask {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyyMMdd-HH:mm").withZone(DateTimeZones.LONDON);
    private static final Pattern FILEDATE = Pattern.compile("^.*/(\\d+)_.*$");

    private final PaChannelMap channelMap = new PaChannelMap();
    private final AtomicInteger processed = new AtomicInteger(0);
    private final Set<String> currentlyProcessing = Sets.newHashSet();
    
    private List<Channel> supportedChannels = ImmutableList.of();

    private final ExecutorService executor;
    private final BoundedExecutor boundedQueue;
    private final PaProgrammeDataStore dataStore;
    private final PaChannelProcessJobBuilder jobBuilder;
    private final AdapterLog log;

    public PaBaseProgrammeUpdater(ExecutorService executor, PaChannelProcessJobBuilder jobBuilder, PaProgrammeDataStore dataStore, AdapterLog log) {
        this.executor = executor;
        this.boundedQueue = new BoundedExecutor(executor, 20);
        this.jobBuilder = jobBuilder;
        this.dataStore = dataStore;
        this.log = log;
    }
    
    public PaBaseProgrammeUpdater(PaChannelProcessJobBuilder jobBuilder, PaProgrammeDataStore dataStore, AdapterLog log) {
        this(Executors.newFixedThreadPool(10), jobBuilder, dataStore, log);
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
                        final DateTime scheduleStart = DATE_FORMAT.parseDateTime(matcher.group(1)+"-06:00");
                        
                        log.record(infoEntry().withSource(getClass()).withDescription("Processing file %s with timezone %s", filename, zone.toString()));
                        
                        processed.set(0);
                        
                        unmarshaller.setListener(new Unmarshaller.Listener() {
                            public void beforeUnmarshal(Object target, Object parent) {
                            }

                            public void afterUnmarshal(Object target, Object parent) {
                                int processedCount = processed.incrementAndGet();
                                
                                if (target instanceof ChannelData) {
                                    ChannelData channelData = (ChannelData) target;
                                    Maybe<Channel> channel = channelMap.getChannel(Integer.valueOf(channelData.getChannelId()));
                                    if (channel.hasValue() && isSupported(channel.requireValue())) {
                                        try {
                                            PaChannelData data = new PaChannelData(channel.requireValue(), channelData.getProgData(), scheduleStart, zone, Timestamp.of(fileToProcess.lastModified()));
                                            boundedQueue.submitTask(jobBuilder.buildFor(currentlyProcessing, data));
                                        } catch (Exception e) {
                                            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Exception submit PA channel update job"));
                                        }
                                    }
                                }
                                
                                reportStatus(String.format("%s: %s processed", filename, processedCount));
                            }
                        });
                        reader.parse(fileToProcess.toURI().toString());
                    }
                } catch (Exception e) {
                    log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Error processing file " + file.toString()));
                }
            }
        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Exception running PA updater"));
        } finally {
            processed.set(0);
        }
    }

    public static class PaChannelData {
        
        private final Channel channel;
        private final Iterable<ProgData> programmes;
        private final DateTime day;
        private final DateTimeZone zone;
        private final Timestamp lastUpdated;

        public PaChannelData(Channel channel, Iterable<ProgData> programmes, DateTime day, DateTimeZone zone, Timestamp lastUpdated) {
            this.channel = channel;
            this.programmes = programmes;
            this.day = day;
            this.zone = zone;
            this.lastUpdated = lastUpdated;
        }

        public Channel channel() {
            return channel;
        }

        public Iterable<ProgData> programmes() {
            return programmes;
        }

        public DateTime day() {
            return day;
        }

        public DateTimeZone zone() {
            return zone;
        }

        public Timestamp lastUpdated() {
            return lastUpdated;
        }
        
    }

    protected static DateTimeZone getTimeZone(String date) {
        String timezoneDateString = date + "-11:00";
        DateTime timezoneDateTime = DATE_FORMAT.parseDateTime(timezoneDateString);
        DateTimeZone zone = timezoneDateTime.getZone();
        return DateTimeZone.forOffsetMillis(zone.getOffset(timezoneDateTime));
    }
}
