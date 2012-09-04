package org.atlasapi.query;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.query.content.fuzzy.RemoteFuzzySearcher;
import org.atlasapi.query.content.search.ContentResolvingSearcher;
import org.atlasapi.query.content.search.DummySearcher;
import org.atlasapi.search.ContentSearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Strings;

@Configuration
public class SearchModule {

    private @Value("${atlas.search.host}") String searchHost;
    
    private @Autowired KnownTypeQueryExecutor queryExecutor;
    
    @PostConstruct
    public void setExecutor() {
        SearchResolver searchResolver = searchResolver();
        if (searchResolver instanceof ContentResolvingSearcher) {
            ((ContentResolvingSearcher)searchResolver).setExecutor(queryExecutor);
        }
    }
    
    @Bean
    SearchResolver searchResolver() {
        if (!Strings.isNullOrEmpty(searchHost)) {
            ContentSearcher titleSearcher = new RemoteFuzzySearcher(searchHost);
            return new ContentResolvingSearcher(titleSearcher, null);
        }

        return new DummySearcher();
    }

}
