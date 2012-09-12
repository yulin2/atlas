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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;

@Configuration
public class SearchModule {

    private @Value("${atlas.search.host}")
    String searchHost;
    private @Autowired
    org.atlasapi.persistence.content.ContentSearcher contentSearcher;
    private @Autowired
    KnownTypeQueryExecutor queryExecutor;

    @PostConstruct
    public void setKnownTypeQueryExecutor() {
        SearchResolver searchResolver = v2SearchResolver();
        if (searchResolver instanceof ContentResolvingSearcher) {
            ((ContentResolvingSearcher) searchResolver).setExecutor(queryExecutor);
        }
        searchResolver = v4SearchResolver();
        if (searchResolver instanceof org.atlasapi.query.v4.search.support.ContentResolvingSearcher) {
            ((org.atlasapi.query.v4.search.support.ContentResolvingSearcher) searchResolver).setResolver(queryExecutor);
        }
    }

    @Bean
    @Primary
    @Qualifier("v2")
    public SearchResolver v2SearchResolver() {
        if (!Strings.isNullOrEmpty(searchHost)) {
            ContentSearcher titleSearcher = new RemoteFuzzySearcher(searchHost);
            return new ContentResolvingSearcher(titleSearcher, null);
        } else {
            return new DummySearcher();
        }
    }

    @Bean
    @Qualifier("v4")
    public SearchResolver v4SearchResolver() {
        // FIXME externalize timeout
        return new org.atlasapi.query.v4.search.support.ContentResolvingSearcher(contentSearcher, null, 60000);
    }
}
