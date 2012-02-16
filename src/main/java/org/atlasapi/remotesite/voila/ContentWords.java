package org.atlasapi.remotesite.voila;

import java.util.List;

public class ContentWords {
    
    public static class ContentWordsList {
        
        private List<ContentWords> results;
        
        public List<ContentWords> getResults() {
            return results;
        }
        
        public void setResults(List<ContentWords> results) {
            this.results = results;
        }
        
    }

    private String contentId;
    private List<String> words;
    private String uri;
    
    public String getContentId() {
        return this.contentId;
    }
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }
    public List<String> getWords() {
        return this.words;
    }
    public void setWords(List<String> words) {
        this.words = words;
    }
    public String getUri() {
        return this.uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }
    
    
    
}
