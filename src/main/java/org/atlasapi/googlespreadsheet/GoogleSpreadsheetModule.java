package org.atlasapi.googlespreadsheet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;

@Configuration
public class GoogleSpreadsheetModule {

    private @Value("${google.spreadsheet.client.id}") String clientId;
    private @Value("${google.spreadsheet.client.secret}") String clientSecret;
    private @Value("${google.spreadsheet.refresh.token}") String refreshToken;
    private @Value("${google.spreadsheet.access.token}") String accessToken;
    
    @Bean
    public ClientCredentials clientCredentials() {
        return new ClientCredentials(clientId, clientSecret, refreshToken, accessToken);
    }
    
    @Bean
    public GoogleOAuth googleOAuth() {
        return new GoogleOAuth(clientCredentials(), new NetHttpTransport(), new JacksonFactory());
    }
    
    @Bean
    public GoogleSpreadsheetService googleSpreadsheetService() {
        return new GoogleSpreadsheetService(googleOAuth().googleCredentials());
    }
    
    @Bean
    public SpreadsheetFetcher spreadsheetFetcher() {
        return new SpreadsheetFetcher(googleSpreadsheetService().getSpreadsheetService());
    }
    
}
