package org.atlasapi.googlespreadsheet;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

public class GoogleOAuth {

    private final ClientCredentials clientCredentials;
    private final HttpTransport httpTransport;
    private final JsonFactory jsonFactory;
    
    public GoogleOAuth(ClientCredentials clientCredentials, 
            HttpTransport httpTransport, JsonFactory jsonFactory) {
        this.clientCredentials = checkNotNull(clientCredentials);
        this.httpTransport = checkNotNull(httpTransport);
        this.jsonFactory = checkNotNull(jsonFactory);
    }
    
    public GoogleCredential googleCredentials() {
        GoogleCredential credential = new GoogleCredential.Builder()
        .setJsonFactory(jsonFactory)
        .setTransport(httpTransport)
        .setClientAuthentication(new ClientParametersAuthentication(clientCredentials.getClientId(), 
                clientCredentials.getClientSecret()))
        .build();
    
        credential.setRefreshToken(clientCredentials.getRefreshToken());
        credential.setAccessToken(clientCredentials.getAccessToken());
        
        return credential;
    }
    
}
