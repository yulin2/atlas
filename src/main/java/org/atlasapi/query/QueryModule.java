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

import org.atlasapi.media.content.Content;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.persistence.AtlasPersistenceModule;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.query.common.ContextualQueryExecutor;
import org.atlasapi.query.common.QueryExecutor;
import org.atlasapi.query.v4.schedule.IndexBackedScheduleQueryExecutor;
import org.atlasapi.query.v4.schedule.ScheduleQueryExecutor;
import org.atlasapi.query.v4.search.support.ContentResolvingSearcher;
import org.atlasapi.query.v4.topic.IndexBackedTopicQueryExecutor;
import org.atlasapi.query.v4.topic.TopicContentQueryExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;

@Configuration
//@Import(EquivModule.class)
public class QueryModule {

    private @Autowired AtlasPersistenceModule persistenceModule;

    @Bean ScheduleQueryExecutor scheduleQueryExecutor() {
        return new IndexBackedScheduleQueryExecutor(persistenceModule.scheduleIndex(), persistenceModule.contentStore());
    }
    
    @Bean QueryExecutor<Topic> topicQueryExecutor() {
        return new IndexBackedTopicQueryExecutor(persistenceModule.topicIndex(), persistenceModule.topicStore());
    }
    
    @Bean
    public ContextualQueryExecutor<Topic, Content> topicContentQueryExecutor() {
        return new TopicContentQueryExecutor(persistenceModule.topicStore(), persistenceModule.contentIndex(), persistenceModule.contentStore());
    }

    @Bean
    public SearchResolver v4SearchResolver() {
        // FIXME externalize timeout
        return new ContentResolvingSearcher(persistenceModule.contentSearcher(), persistenceModule.contentStore(), 60000);
    }

    @Bean
    @Qualifier("v2")
    public SearchResolver v2SearchResolver() {
        if (!Strings.isNullOrEmpty(searchHost)) {
            ContentSearcher titleSearcher = new RemoteFuzzySearcher(searchHost);
            return new ContentResolvingSearcher(titleSearcher, queryExecutor());
        }

        return new DummySearcher();
    }
    
    @Bean
    @Qualifier("v4")
    public SearchResolver v4SearchResolver() {
        // FIXME externalize timeout
        return new org.atlasapi.query.v4.search.support.ContentResolvingSearcher(contentSearcher, queryExecutor(), 60000);
    }
}
