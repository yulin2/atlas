package org.atlasapi.remotesite.globalimageworks;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.atlasapi.media.entity.Publisher.GLOBALIMAGEWORKS;
import static org.atlasapi.remotesite.globalimageworks.GlobalImageSpreadsheetColumn.ID;

import org.atlasapi.spreadsheet.SpreadsheetFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.metabroadcast.common.properties.Configurer;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.scheduling.UpdateProgress;

public class GlobalImageUpdateTask extends ScheduledTask {

private static final Logger log = LoggerFactory.getLogger(GlobalImageUpdateTask.class);
    
    private final String spreadsheetTitle = Configurer.get("spreadsheet.title").get();
    private final SpreadsheetFetcher spreadsheetFetcher;
    private final GlobalImageAdapter adapter;
    private final GlobalImageDataRowHandler dataHandler;
    
    public GlobalImageUpdateTask(SpreadsheetFetcher spreadsheetFetcher, 
            GlobalImageDataRowHandler dataHandler, GlobalImageAdapter adapter) {
        this.spreadsheetFetcher = checkNotNull(spreadsheetFetcher);
        this.dataHandler = checkNotNull(dataHandler);
        this.adapter = checkNotNull(adapter);
    }
    
    @Override
    protected void runTask() {
        try {
            ListFeed data = fetchData();
            GlobalImageDataProcessor<UpdateProgress> processor = processor();
            for (ListEntry row : data.getEntries()) {
                isGlobalImage(processor, row.getCustomElements());
            }
            
            reportStatus(processor.getResult().toString());
            
        } catch (Exception e) {
            reportStatus(e.getMessage());
            throw Throwables.propagate(e);
        }
    }
    
    private ListFeed fetchData() {
        SpreadsheetEntry spreadsheet = Iterables.getOnlyElement(spreadsheetFetcher.getSpreadsheetByTitle(spreadsheetTitle));
        WorksheetEntry worksheet = Iterables.getOnlyElement(spreadsheetFetcher.getWorksheetsFromSpreadsheet(spreadsheet));
        return spreadsheetFetcher.getDataFromWorksheet(worksheet);
    }
    
    //needed because same spreadsheet contains multiple sources
    private void isGlobalImage(GlobalImageDataProcessor<UpdateProgress> processor, CustomElementCollection customElements) {
        if (customElements.getValue(GlobalImageSpreadsheetColumn.SOURCE.getValue()).equals(GLOBALIMAGEWORKS.title())) {
            processor.process(customElements);
        }
    }

    private GlobalImageDataProcessor<UpdateProgress> processor() {
        return new GlobalImageDataProcessor<UpdateProgress>() {
            
            UpdateProgress progress = UpdateProgress.START;
            
            @Override
            public boolean process(CustomElementCollection customElements) {
                try {
                    GlobalImageDataRow row = adapter.globalImageDataRow(customElements);
                    dataHandler.handle(row);
                    progress = progress.reduce(UpdateProgress.SUCCESS);
                } catch (Exception e) {
                    log.warn("Row: " + customElements.getValue(ID.getValue()), e);
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
