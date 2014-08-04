package org.atlasapi.remotesite.globalimageworks;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.spreadsheet.SpreadsheetFetcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.metabroadcast.common.scheduling.ScheduledTask;

@RunWith(MockitoJUnitRunner.class)
public class GlobalImageUpdateTaskTest {

    private final SpreadsheetFetcher spreadsheetFetcher = mock(SpreadsheetFetcher.class);
    private final GlobalImageAdapter adapter = mock(GlobalImageAdapter.class);
    private final GlobalImageDataRowHandler dataHandler = mock(GlobalImageDataRowHandler.class);
    private final ScheduledTask task = new GlobalImageUpdateTask(spreadsheetFetcher, dataHandler, adapter);
    
    @Test
    public void test() {
        SpreadsheetEntry spreadsheet = new SpreadsheetEntry();
        WorksheetEntry worksheet = new WorksheetEntry();
        ListFeed feed = new ListFeed();
        ListEntry entry = new ListEntry();
        entry.getCustomElements().setValueLocal(GlobalImageSpreadsheetColumn.SOURCE.getValue(), Publisher.GLOBALIMAGEWORKS.title());
        feed.setEntries(ImmutableList.of(entry));
        
        when(spreadsheetFetcher.getSpreadsheetByTitle(Matchers.anyString())).thenReturn(ImmutableList.of(spreadsheet));
        when(spreadsheetFetcher.getWorksheetsFromSpreadsheet(spreadsheet)).thenReturn(ImmutableList.of(worksheet));
        when(spreadsheetFetcher.getDataFromWorksheet(worksheet)).thenReturn(feed);
        
        task.run();
        verify(adapter, times(1)).globalImageDataRow(Iterables.getOnlyElement(feed.getEntries()).getCustomElements());
        verify(dataHandler, times(1)).handle(Matchers.any(GlobalImageDataRow.class));
    }
    
}
