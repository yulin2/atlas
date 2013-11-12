package org.atlasapi.remotesite.wikipedia;

import net.sourceforge.jwbf.core.contentRep.Article;

public interface ArticleFetcher {
    public Article fetchArticle(String title);    
}
