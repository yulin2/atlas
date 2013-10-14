package org.atlasapi.application.model.auth;

import java.net.MalformedURLException;
import java.net.URL;


public class OAuthRequest {
    private final URL authUrl;
    private final String token;
    
    private OAuthRequest(URL authUrl, String token) {
        this.authUrl = authUrl;
        this.token = token;
    }
    
    public URL getAuthUrl() {
        return authUrl;
    }
    
    public String getToken() {
        return token;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private URL authUrl;
        private String token;
        
        public Builder withAuthUrl(URL authUrl) {
            this.authUrl = authUrl;
            return this;
        }
        
        public Builder withToken(String token) {
            this.token = token;
            return this;
        }
        
        public Builder withAuthUrl(String authUrl) throws MalformedURLException {
            this.authUrl = new URL(authUrl);
            return this;
        }
        
        public OAuthRequest build() {
            return new OAuthRequest(this.authUrl, this.token);
        }
    }
}
