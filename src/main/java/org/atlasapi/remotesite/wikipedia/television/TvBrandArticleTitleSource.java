package org.atlasapi.remotesite.wikipedia.television;

public interface TvBrandArticleTitleSource {
    
    public class TvIndexingException extends Exception {
        public TvIndexingException(Exception ex) {
            super(ex);
        }
    }
    
    Iterable<String> getAllTvBrandArticleTitles() throws TvIndexingException;
}
