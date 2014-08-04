package org.atlasapi.spreadsheet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;

@Configuration
public class GoogleSpreadsheetModule {

    private @Value("${client.id}") String clientId;
    private @Value("${client.secret}") String clientSecret;
    private @Value("${refresh.token}") String refreshToken;
    private @Value("${access.token}") String accessToken;
    
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
