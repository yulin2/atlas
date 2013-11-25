package org.atlasapi.remotesite.wikipedia;

public interface ArticleFetcher {
    Article fetchArticle(String title);    
}
