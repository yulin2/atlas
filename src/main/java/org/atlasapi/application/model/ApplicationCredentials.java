package org.atlasapi.application.model;

import static com.google.common.base.Preconditions.checkNotNull;

public class ApplicationCredentials {

	private final String apiKey;
	
	public ApplicationCredentials(String apiKey) {
        this.apiKey = checkNotNull(apiKey);
    }
	
	public String getApiKey() {
		return apiKey;
	}
	
	public Builder copy() {
	    return new Builder().withApiKey(apiKey);
	}
	
	public static Builder builder() {
	    return new Builder();
	}
	
	public static class Builder {
	    private String apiKey;
        
        public Builder withApiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }
	    
	    public ApplicationCredentials build() {
	        return new ApplicationCredentials(this.apiKey);
	    }
	}
}
