package org.atlasapi.application.persistence;

import java.util.List;
import java.util.Map;
import org.atlasapi.application.SourceStatus;
import org.atlasapi.application.SourceStatus.SourceState;
import org.atlasapi.application.model.ApplicationSources;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ApplicationSourcesTranslator {

	public static final String STATE_KEY = "state";
    public static final String PUBLISHER_KEY = "publisher";
    public static final String SOURCES_KEY = "sources";
	public static final String PRECEDENCE_KEY = "precedence";
	public static final String WRITABLE_KEY = "writable";

	public DBObject toDBObject(ApplicationSources configuration) {
		BasicDBObject dbo = new BasicDBObject();
//		
//		TranslatorUtils.from(dbo, SOURCES_KEY, sourceStatusesToList(configuration.sourceStatuses()));
//		
//		if (configuration.precedenceEnabled()) { 
//			TranslatorUtils.fromList(dbo, Lists.transform(configuration.precedence(), Publisher.TO_KEY), PRECEDENCE_KEY);
//		} else {
//			dbo.put(PRECEDENCE_KEY, null);
//		}
//		
//		dbo.put(WRITABLE_KEY, Lists.transform(configuration.writableSources().asList(), Publisher.TO_KEY));
//		
		return dbo;
	}
	
//	private BasicDBList sourceStatusesToList(Map<Publisher, SourceStatus> sourceStatuses) {
//	    BasicDBList statuses = new BasicDBList();
//	    for (Entry<Publisher, SourceStatus> sourceStatus : sourceStatuses.entrySet()) {
//            statuses.add(new BasicDBObject(ImmutableMap.of(
//                    PUBLISHER_KEY, sourceStatus.getKey().key(), 
//                    STATE_KEY, sourceStatus.getValue().getState().toString().toLowerCase(),
//                    "enabled", sourceStatus.getValue().isEnabled()
//            )));
//        }
//        return statuses;
//    }
	
	public ApplicationSources fromDBObject(DBObject dbo) {
	    List<DBObject> statusDbos = TranslatorUtils.toDBObjectList(dbo, SOURCES_KEY);
        Map<Publisher, SourceStatus> sourceStatuses = sourceStatusesFrom(statusDbos);
	
		List<Publisher> precedence = sourcePrecedenceFrom(dbo);

		List<String> writableKeys = TranslatorUtils.toList(dbo, WRITABLE_KEY);
        Iterable<Publisher> writableSources = Lists.transform(writableKeys, Publisher.FROM_KEY);
 		
		boolean precedenceFlag = precedence != null && !precedence.isEmpty();
		return ApplicationSources.builder()
		        .withPrecedence(precedenceFlag)
		        .withReads(sourceStatuses)
		        .withWrites(writableSources)
		        .build();
		
	}

    private List<Publisher> sourcePrecedenceFrom(DBObject dbo) {
        if (dbo.get(PRECEDENCE_KEY) == null) {
            return null;
        }
        List<String> sourceKeys = TranslatorUtils.toList(dbo, PRECEDENCE_KEY);
        return Lists.transform(sourceKeys, Publisher.FROM_KEY);
    }
	
    private Map<Publisher, SourceStatus> sourceStatusesFrom(List<DBObject> list) {
        Builder<Publisher, SourceStatus> builder = ImmutableMap.builder();
        for (DBObject dbo : list) {
            builder.put(
                Publisher.fromKey(TranslatorUtils.toString(dbo, PUBLISHER_KEY)).requireValue(),
                sourceStatusFrom(dbo)
            );
        }
        return builder.build();
    }

    private SourceStatus sourceStatusFrom(DBObject dbo) {
        if (TranslatorUtils.toBoolean(dbo, "enabled")) {
            return SourceStatus.AVAILABLE_ENABLED;
        }
        switch (SourceState.valueOf(TranslatorUtils.toString(dbo, STATE_KEY).toUpperCase())) {
            case AVAILABLE:
                return SourceStatus.AVAILABLE_DISABLED;
            case REQUESTED:
                return SourceStatus.REQUESTED;
            case REVOKED:
                return SourceStatus.REVOKED;
            default:
                return SourceStatus.UNAVAILABLE;
        }
    }

}
