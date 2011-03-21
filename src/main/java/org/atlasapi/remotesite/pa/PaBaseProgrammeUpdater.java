package org.atlasapi.remotesite.pa;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.pa.bindings.ChannelData;
import org.atlasapi.remotesite.pa.bindings.ProgData;
import org.atlasapi.util.concurrency.BoundedExecutor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.xml.sax.XMLReader;

import com.metabroadcast.common.time.DateTimeZones;

public abstract class PaBaseProgrammeUpdater implements Runnable {

    private static final Pattern FILEDATE = Pattern.compile("^.*/(\\d+)_.*$");
    
    private final AdapterLog log;
    private boolean isRunning = false;

    private final PaProgrammeProcessor processor;
    private final ExecutorService executor = Executors.newFixedThreadPool(8);
    private final BoundedExecutor boundedQueue = new BoundedExecutor(executor, 15);

    public PaBaseProgrammeUpdater(PaProgrammeProcessor processor, AdapterLog log) {
        this.processor = processor;
        this.log = log;
    }

    public boolean isRunning() {
        return isRunning;
    }
    
    @Override
    public abstract void run();
    
    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
    
    protected void processFiles(Iterable<File> files) {
        if (isRunning) {
            throw new IllegalStateException("Already running");
        }

        isRunning = true;
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
                        final DateTimeZone zone = getTimeZone(matcher.group(1));
                        log.record(new AdapterLogEntry(Severity.INFO).withSource(PaBaseProgrammeUpdater.class).withDescription("Processing file "+filename+" with timezone "+zone.toString()));
                        final AtomicInteger recordsProcessed = new AtomicInteger();
                        
                        unmarshaller.setListener(new Unmarshaller.Listener() {
                            public void beforeUnmarshal(Object target, Object parent) {
                            }

                            public void afterUnmarshal(Object target, Object parent) {
                                if (target instanceof ProgData) {
                                    try {
                                        boundedQueue.submitTask(new ProcessProgrammeJob((ProgData) target, (ChannelData) parent, zone));
                                    } catch (InterruptedException e) {
                                        log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaBaseProgrammeUpdater.class));
                                    }
                                    //new ProcessProgrammeJob((ProgData) target, (ChannelData) parent, zone).run();
                                    int processed = recordsProcessed.incrementAndGet();
                                    
                                    if (processed % 100 == 0) {
                                        log.record(new AdapterLogEntry(Severity.INFO).withSource(PaBaseProgrammeUpdater.class).withDescription("Processed "+processed+" programmes from "+filename));
                                    }
                                }
                            }
                        });

                        reader.parse(filename);
                    }
                } catch (Exception e) {
                    log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaBaseProgrammeUpdater.class).withDescription("Error processing file " + file.toString()));
                }
            }
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaBaseProgrammeUpdater.class));
        } finally {
            isRunning = false;
        }
    }  
    
    protected static DateTimeZone getTimeZone(String date) {
        String timezoneDateString = date + "-11:00";
        DateTime timezoneDateTime = DateTimeFormat.forPattern("yyyyMMdd-HH:mm").withZone(DateTimeZones.LONDON).parseDateTime(timezoneDateString);
        DateTimeZone zone = timezoneDateTime.getZone();
        return DateTimeZone.forOffsetMillis(zone.getOffset(timezoneDateTime));
    }
    
    class ProcessProgrammeJob implements Runnable {
        
        private final ProgData progData;
        private final ChannelData channelData;
        private final DateTimeZone zone;

        public ProcessProgrammeJob(ProgData progData, ChannelData channelData, DateTimeZone zone) {
            this.progData = progData;
            this.channelData = channelData;
            this.zone = zone;
        }

        @Override
        public void run() {
            try {
                if (progData != null) {
                    processor.process(progData, channelData, zone);
                }
            } catch (Exception e) {
                log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(PaBaseProgrammeUpdater.class).withDescription("Error processing programme " + progData));
            }
        }
    }
}
