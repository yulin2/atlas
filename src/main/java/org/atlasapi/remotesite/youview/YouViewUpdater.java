package org.atlasapi.remotesite.youview;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import org.atlasapi.remotesite.redux.UpdateProgress;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.metabroadcast.common.scheduling.ScheduledTask;


public class YouViewUpdater extends ScheduledTask {

    private static final String ATOM_PREFIX = "atom";
    private static final String ENTRY_KEY = "entry";
    private final YouViewScheduleFetcher fetcher;
    private final YouViewXmlElementHandler elementHandler;
    private final Duration plus;
    private final Duration minus;
    private final Logger log = LoggerFactory.getLogger(YouViewUpdater.class);
    
    public YouViewUpdater(YouViewScheduleFetcher fetcher, YouViewXmlElementHandler elementHandler, Duration minus, Duration plus) {
        this.fetcher = fetcher;
        this.elementHandler = elementHandler;
        this.minus = minus;
        this.plus = plus;
    }
    
    @Override
    protected void runTask() {
        try {
            DateTime midnightToday = new DateTime(DateMidnight.now());
            DateTime startTime = midnightToday.minus(minus);
            DateTime endTime = midnightToday.plus(plus);
            
            YouViewDataProcessor<UpdateProgress> processor = processor();
            
            while (startTime.isBefore(endTime)) {

                Document xml = fetcher.getSchedule(startTime, startTime.plusDays(1));
                Element root = xml.getRootElement();
                Elements entries = root.getChildElements(ENTRY_KEY, root.getNamespaceURI(ATOM_PREFIX));

                for (int i = 0; i < entries.size(); i++) {
                    processor.process(entries.get(i));
                }
                startTime = startTime.plusDays(1);
            }

            reportStatus(processor.getResult().toString());
        } catch (Exception e) {
            log.error("Exception when processing YouView schedule", e);
            Throwables.propagate(e);
        }

    }
    
    private YouViewDataProcessor<UpdateProgress> processor() {
        return new YouViewDataProcessor<UpdateProgress>() {
            
            UpdateProgress progress = UpdateProgress.START;
            
            @Override
            public boolean process(Element element) {
                try {
                    elementHandler.handle(element);
                    progress = progress.reduce(UpdateProgress.SUCCESS);
                } catch (Exception e) {
                    log.warn(element.getLocalName() , e);
                    progress = progress.reduce(UpdateProgress.FAILURE);
                }
                reportStatus(progress.toString());
                return shouldContinue();
            }

            @Override
            public UpdateProgress getResult() {
                return progress;
            }
        };
    }

}
