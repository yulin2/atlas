package org.atlasapi.remotesite.lovefilm;

import static org.atlasapi.remotesite.lovefilm.LoveFilmCsvColumn.SKU;

import org.atlasapi.feeds.utils.UpdateProgress;
import org.atlasapi.remotesite.lovefilm.LoveFilmData.LoveFilmDataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class LoveFilmCsvUpdateTask extends ScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(LoveFilmCsvUpdateTask.class);
    
    private final LoveFilmFileUpdater updater;
    private final LoveFilmDataRowHandler dataHandler;
    private final LoveFilmFileStore store;
    private final LoveFilmBrandProcessor brandProcessor;

    public LoveFilmCsvUpdateTask(LoveFilmFileUpdater updater, LoveFilmFileStore store, LoveFilmDataRowHandler dataHandler, LoveFilmBrandProcessor brandProcessor) {
        this.updater = updater;
        this.store = store;
        this.dataHandler = dataHandler;
        this.brandProcessor = brandProcessor;
    }
    
    @Override
    protected void runTask() {
        try {
            updater.update();
            
            dataHandler.prepare();
            UpdateProgress progress = store.fetchLatestData().processData(preprocessor());
            dataHandler.finish();
            
            dataHandler.prepare();
            progress = store.fetchLatestData().processData(processor());
            dataHandler.finish();
            
            reportStatus(progress.toString());
            
        } catch (Exception e) {
            reportStatus(e.getMessage());
            throw Throwables.propagate(e);
        }
    }

    private LoveFilmDataProcessor<UpdateProgress> preprocessor() {
        return new LoveFilmDataProcessor<UpdateProgress>() {
            
            UpdateProgress progress = UpdateProgress.START;
            
            @Override
            public boolean process(LoveFilmDataRow row) {
                try {
                    brandProcessor.handle(row);
                    progress = progress.reduce(UpdateProgress.SUCCESS);
                } catch (Exception e) {
                    log.warn("Row: " + SKU.valueFrom(row), e);
                    progress = progress.reduce(UpdateProgress.FAILURE);
                }
                reportStatus("Brand Preprocessor: " + progress.toString());
                return shouldContinue();
            }

            @Override
            public UpdateProgress getResult() {
                return progress;
            }
        };
    }

    private LoveFilmDataProcessor<UpdateProgress> processor() {
        return new LoveFilmDataProcessor<UpdateProgress>() {
            
            UpdateProgress progress = UpdateProgress.START;
            
            @Override
            public boolean process(LoveFilmDataRow row) {
                try {
                    dataHandler.handle(row);
                    progress = progress.reduce(UpdateProgress.SUCCESS);
                } catch (Exception e) {
                    log.warn("Row: " + SKU.valueFrom(row), e);
                    progress = progress.reduce(UpdateProgress.FAILURE);
                }
                reportStatus("Ingesting content: " + progress.toString());
                return shouldContinue();
            }

            @Override
            public UpdateProgress getResult() {
                return progress;
            }
        };
    }

}
