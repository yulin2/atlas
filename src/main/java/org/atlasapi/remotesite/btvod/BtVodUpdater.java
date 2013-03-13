package org.atlasapi.remotesite.btvod;

import nu.xom.Document;
import nu.xom.Element;

import org.atlasapi.feeds.utils.UpdateProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class BtVodUpdater extends ScheduledTask {
    
    private final Logger log = LoggerFactory.getLogger(BtVodUpdater.class);

    private final BtVodXmlElementHandler elementHandler;
    private final BtVodContentFetcher fetcher;
    
    public BtVodUpdater(BtVodXmlElementHandler elementHandler, BtVodContentFetcher fetcher) {
        this.elementHandler = elementHandler;
        this.fetcher = fetcher;
    }
    
    @Override
    protected void runTask() {
        try {
            fetcher.getLatestContentList();

            BtVodDataProcessor<UpdateProgress> processor = processor();

            elementHandler.prepare();

            Document contentXml = fetcher.getContent();
            while(contentXml != null) {
                processor.process(contentXml.getRootElement());
                contentXml = fetcher.getContent();
            }

            elementHandler.finish();

            reportStatus(processor.getResult().toString());
        } catch (Exception e) {
            log.error("Exception when processing BT VOD catalog:", e);
            Throwables.propagate(e);
        }
    }

    private BtVodDataProcessor<UpdateProgress> processor() {
        return new BtVodDataProcessor<UpdateProgress>() {
            
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
