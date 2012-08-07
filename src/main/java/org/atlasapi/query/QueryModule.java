/* Copyright 2010 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.query;

import static org.atlasapi.media.entity.Publisher.FACEBOOK;

import org.atlasapi.equiv.EquivModule;
import org.atlasapi.equiv.query.MergeOnOutputQueryExecutor;
import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.FilterScheduleOnlyKnownTypeContentResolver;
import org.atlasapi.persistence.content.KnownTypeContentResolver;
import org.atlasapi.persistence.content.SimpleKnownTypeContentResolver;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
import org.atlasapi.query.content.ApplicationConfigurationQueryExecutor;
import org.atlasapi.query.content.CurieResolvingQueryExecutor;
import org.atlasapi.query.content.LookupResolvingQueryExecutor;
import org.atlasapi.query.content.UriFetchingQueryExecutor;
import org.atlasapi.query.uri.canonical.CanonicalisingFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.google.common.collect.ImmutableSet;

@Configuration
@Import(EquivModule.class)
public class QueryModule {

	@Autowired
    @Qualifier("remoteSiteContentResolver")
    private CanonicalisingFetcher localOrRemoteFetcher;
    @Autowired
    private LookupEntryStore mongoStore;
    @Autowired
    private KnownTypeContentResolver mongoResolver;
    @Autowired @Qualifier(value="cassandra")
    private ContentResolver cassandraResolver;
    @Autowired @Qualifier("contentUpdater") 
    private ContentEquivalenceUpdater<Content> equivUpdater;
    //
    @Value("${applications.enabled}")
    private String applicationsEnabled;
    @Value("${atlas.search.host}")
    private String searchHost;

    @Bean
    public KnownTypeQueryExecutor queryExecutor() {

        KnownTypeQueryExecutor queryExecutor = new LookupResolvingQueryExecutor(new SimpleKnownTypeContentResolver(cassandraResolver),
                new FilterScheduleOnlyKnownTypeContentResolver(mongoResolver),
                mongoStore);

        queryExecutor = new UriFetchingQueryExecutor(localOrRemoteFetcher, queryExecutor, equivUpdater, ImmutableSet.of(FACEBOOK));

        queryExecutor = new CurieResolvingQueryExecutor(queryExecutor);

        queryExecutor = new MergeOnOutputQueryExecutor(queryExecutor);

        return Boolean.parseBoolean(applicationsEnabled) ? new ApplicationConfigurationQueryExecutor(queryExecutor) : queryExecutor;
    }

}
