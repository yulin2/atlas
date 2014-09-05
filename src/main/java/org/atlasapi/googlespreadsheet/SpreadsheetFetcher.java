package org.atlasapi.googlespreadsheet;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URL;
import java.util.List;

import com.google.gdata.client.spreadsheet.SpreadsheetQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;

public class SpreadsheetFetcher {

    private static final String SPREADSHEET_URL = "https://spreadsheets.google.com/feeds/spreadsheets/private/full";
    
    private final SpreadsheetService service;
    
    public SpreadsheetFetcher(SpreadsheetService service) {
        this.service = checkNotNull(service);
    }   
    
    public ListFeed getDataFromWorksheet(WorksheetEntry worksheetEntry) {
        try {
            return service.getFeed(worksheetEntry.getListFeedUrl(), ListFeed.class);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error getting data from worksheet ", 
                    worksheetEntry.getTitle().getPlainText()), e);
        }
    }
    
    public List<WorksheetEntry> getWorksheetsFromSpreadsheet(SpreadsheetEntry spreadsheet) {
        try {
            WorksheetFeed feed = service.getFeed(spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
            return feed.getEntries();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error getting data from spreadsheet ", 
                    spreadsheet.getTitle().getPlainText()), e);
        }
    }
    
    public List<SpreadsheetEntry> getSpreadsheetByTitle(String spreadsheetTitle) {
        try {
            SpreadsheetQuery query = new SpreadsheetQuery(new URL(SPREADSHEET_URL));
            query.setTitleQuery(spreadsheetTitle);
            
            SpreadsheetFeed feed = service.getFeed(query, SpreadsheetFeed.class);
            return feed.getEntries();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error getting spreadsheet ", spreadsheetTitle), e);
        }
    }
    
}
