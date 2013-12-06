package org.atlasapi.remotesite.wikipedia;

public interface ArticleFetcher {
    public static class FetchFailedException extends Exception {}
    
    Article fetchArticle(String title) throws FetchFailedException;
}
