package org.atlasapi.logging;

import org.atlasapi.logging.www.LogViewingController;
import org.atlasapi.persistence.logging.MongoLoggingAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.mongodb.Mongo;

@Configuration
public class AtlasLoggingModule {
	
	private @Autowired Mongo mongo;
	
	public @Bean MongoLoggingAdapter adapterLog() {
		return new MongoLoggingAdapter(new DatabasedMongo(mongo, "atlas"));
	}
	
	public @Bean LogViewingController logView() {
		return new LogViewingController(adapterLog());
	}
}
