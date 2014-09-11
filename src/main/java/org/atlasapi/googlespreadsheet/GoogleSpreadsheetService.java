package org.atlasapi.googlespreadsheet;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.gdata.client.spreadsheet.SpreadsheetService;

public class GoogleSpreadsheetService {

    private static final String SERVICE = "KM";
    
    private final GoogleCredential googleCredential;
    
    public GoogleSpreadsheetService(GoogleCredential googleCredential) {
        this.googleCredential = checkNotNull(googleCredential);
    }
    
    public SpreadsheetService getSpreadsheetService() {
        SpreadsheetService service = new SpreadsheetService(SERVICE);
        service.setOAuth2Credentials(googleCredential);
        return service;
    }
    
}
