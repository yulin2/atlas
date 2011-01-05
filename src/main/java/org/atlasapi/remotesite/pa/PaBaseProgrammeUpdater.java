package org.atlasapi.remotesite.pa;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.pa.bindings.ChannelData;
import org.atlasapi.remotesite.pa.bindings.ProgData;
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

    public PaBaseProgrammeUpdater(PaProgrammeProcessor processor, AdapterLog log) {
        this.processor = processor;
        this.log = log;
    }

    public boolean isRunning() {
        return isRunning;
    }
    
    @Override
    public abstract void run();
    
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
                    String filename = file.toURI().toString();
                    Matcher matcher = FILEDATE.matcher(filename);
                    if (matcher.matches()) {
                        final DateTimeZone zone = getTimeZone(matcher.group(1));
                        log.record(new AdapterLogEntry(Severity.INFO).withSource(PaBaseProgrammeUpdater.class).withDescription("Processing file "+filename+" with timezone "+zone.toString()));
                        
                        unmarshaller.setListener(new Unmarshaller.Listener() {
                            public void beforeUnmarshal(Object target, Object parent) {
                            }

                            public void afterUnmarshal(Object target, Object parent) {
                                if (target instanceof ProgData) {
                                    processor.process((ProgData) target, (ChannelData) parent, zone);
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
}
