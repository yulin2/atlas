package org.atlasapi.remotesite.knowledgemotion;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.googlespreadsheet.SpreadsheetFetcher;
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

public class KnowledgeMotionUpdateTask extends ScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeMotionUpdateTask.class);
    
    private final String spreadsheetTitle = Configurer.get("google.spreadsheet.title").get();
    private final SpreadsheetFetcher spreadsheetFetcher;
    private final KnowledgeMotionAdapter adapter;
    private final KnowledgeMotionDataRowHandler dataHandler;

    public KnowledgeMotionUpdateTask(SpreadsheetFetcher spreadsheetFetcher, 
            KnowledgeMotionDataRowHandler dataHandler, KnowledgeMotionAdapter adapter) {
        this.spreadsheetFetcher = checkNotNull(spreadsheetFetcher);
        this.dataHandler = checkNotNull(dataHandler);
        this.adapter = checkNotNull(adapter);
    }

    @Override
    protected void runTask() {
        try {
            ListFeed data = fetchData();
            KnowledgeMotionDataProcessor<UpdateProgress> processor = processor();
            for (ListEntry row : data.getEntries()) {
                processor.process(row.getCustomElements());
            }

            reportStatus(processor.getResult().toString());

        } catch (Exception e) {
            reportStatus(e.getMessage());
            throw Throwables.propagate(e);
        }
    }

    private ListFeed fetchData() {
        
        SpreadsheetEntry spreadsheet = Iterables.getOnlyElement(spreadsheetFetcher.getSpreadsheetByTitle(getModifiedTitle()));
        WorksheetEntry worksheet = Iterables.getOnlyElement(spreadsheetFetcher.getWorksheetsFromSpreadsheet(spreadsheet));
        return spreadsheetFetcher.getDataFromWorksheet(worksheet);
    }

    //Replace spaces with dashes (something weird happening in jetty)
    private String getModifiedTitle() {
        return spreadsheetTitle.replace("-", " ");
    }
    
    private KnowledgeMotionDataProcessor<UpdateProgress> processor() {
        return new KnowledgeMotionDataProcessor<UpdateProgress>() {

            UpdateProgress progress = UpdateProgress.START;

            @Override
            public boolean process(CustomElementCollection customElements) {
                try {
                    KnowledgeMotionDataRow row = adapter.dataRow(customElements);
                    dataHandler.handle(row);
                    progress = progress.reduce(UpdateProgress.SUCCESS);
                } catch (Exception e) {
                    log.warn("Row: " + customElements.getValue(KnowledgeMotionSpreadsheetColumn.ID.getValue()), e);
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
