package org.atlasapi.remotesite.getty;

import static com.google.common.base.Preconditions.checkNotNull;

public class VideoRequest {

    private final String token;
    private final String searchPhrase;
    private final int itemCount;
    private final int itemStartNumber;
    
    public VideoRequest(String token, String searchPhrase, int itemCount, int itemStartNumber) {
        this.token = checkNotNull(token);
        this.searchPhrase = checkNotNull(searchPhrase);
        this.itemCount = checkNotNull(itemCount);
        this.itemStartNumber = checkNotNull(itemStartNumber);
    }
    
    public String getToken() {
        return token;
    }
    
    public String getSearchPhrase() {
        return searchPhrase;
    }
    
    public int getItemCount() {
        return itemCount;
    }
    
    public int getItemStartNumber() {
        return itemStartNumber;
    }
    
}
