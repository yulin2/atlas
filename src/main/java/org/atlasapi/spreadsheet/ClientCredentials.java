package org.atlasapi.spreadsheet;

import static com.google.common.base.Preconditions.checkNotNull;

public class ClientCredentials {

    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;
    private final String accessToken;
    
    public ClientCredentials(String clientId, String clientSecret, String refreshToken, String accessToken) {
        this.clientId = checkNotNull(clientId);
        this.clientSecret = checkNotNull(clientSecret);
        this.refreshToken = checkNotNull(refreshToken);
        this.accessToken = checkNotNull(accessToken);
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public String getClientSecret() {
        return clientSecret;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
}
