package org.atlasapi.query.content;

import java.net.UnknownHostException;

import org.atlasapi.query.content.fuzzy.MongoFullTextSearcher;
import org.atlasapi.search.model.SearchQuery;
import org.junit.Test;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.mongodb.Mongo;

public class MongoFullTextSearcherTest {

    @Test
    public void testSearch() throws UnknownHostException {
        Mongo mongo = new Mongo("127.0.0.1");
        DatabasedMongo dbMongo = new DatabasedMongo(mongo, "atlas");
        
        MongoFullTextSearcher fts = new MongoFullTextSearcher(dbMongo);
        fts.search(new SearchQuery("foo", null, 1.0f, 1.0f, 1.0f));
    }
    
}
