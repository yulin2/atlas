package org.atlasapi.remotesite.bloomberg;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.atlasapi.remotesite.bloomberg.BloombergSpreadsheetColumn.ID;

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

public class BloombergUpdateTask extends ScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(BloombergUpdateTask.class);
    
    private final String spreadsheetTitle = Configurer.get("spreadsheet.title").get();
    private final SpreadsheetFetcher spreadsheetFetcher;
    private final BloombergAdapter adapter;
    private final BloombergDataRowHandler dataHandler;

    final static String BLOOMBERG_ROW_HEADER = "Bloomberg";

    public BloombergUpdateTask(SpreadsheetFetcher spreadsheetFetcher, 
            BloombergDataRowHandler dataHandler, BloombergAdapter adapter) {
        this.spreadsheetFetcher = checkNotNull(spreadsheetFetcher);
        this.dataHandler = checkNotNull(dataHandler);
        this.adapter = checkNotNull(adapter);
    }
    
    @Override
    protected void runTask() {
        try {
            ListFeed data = fetchData();
            BloombergDataProcessor<UpdateProgress> processor = processor();
            for (ListEntry row : data.getEntries()) {
                isBloomberg(processor, row.getCustomElements());
            }
            
            reportStatus(processor.getResult().toString());
            
        } catch (Exception e) {
            reportStatus(e.getMessage());
            throw Throwables.propagate(e);
        }
    }
    
    private ListFeed fetchData() {
        SpreadsheetEntry spreadsheet = Iterables.getOnlyElement(spreadsheetFetcher.getSpreadsheetByTitle(spreadsheetTitle.replace("-", " ")));
        WorksheetEntry worksheet = Iterables.getOnlyElement(spreadsheetFetcher.getWorksheetsFromSpreadsheet(spreadsheet));
        return spreadsheetFetcher.getDataFromWorksheet(worksheet);
    }
    
    //needed because same spreadsheet contains multiple sources
    private void isBloomberg(BloombergDataProcessor<UpdateProgress> processor, CustomElementCollection customElements) {
        if (BLOOMBERG_ROW_HEADER.equals(customElements.getValue(BloombergSpreadsheetColumn.SOURCE.getValue()))) {
            processor.process(customElements);
        }
    }

    private BloombergDataProcessor<UpdateProgress> processor() {
        return new BloombergDataProcessor<UpdateProgress>() {
            
            UpdateProgress progress = UpdateProgress.START;
            
            @Override
            public boolean process(CustomElementCollection customElements) {
                try {
                    BloombergDataRow row = adapter.bloombergDataRow(customElements);
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
