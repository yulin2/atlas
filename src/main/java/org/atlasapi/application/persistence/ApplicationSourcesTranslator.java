package org.atlasapi.application.persistence;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.atlasapi.application.SourceStatus;
import org.atlasapi.application.SourceStatus.SourceState;
import org.atlasapi.application.model.ApplicationSources;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.BasicDBList;
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

        TranslatorUtils.from(dbo, SOURCES_KEY, sourceStatusesToList(configuration.getReads()));

        if (configuration.isPrecedenceEnabled()) {
            List<String> precedenceOrder = Lists.newLinkedList();
            for (Publisher source : configuration.getReads().keySet()) {
                precedenceOrder.add(source.key());
            }
            TranslatorUtils.fromList(dbo, precedenceOrder, PRECEDENCE_KEY);
        } else {
            dbo.put(PRECEDENCE_KEY, null);
        }

        dbo.put(WRITABLE_KEY, Iterables.transform(configuration.getWrites(), Publisher.TO_KEY));

        return dbo;
    }

    private BasicDBList sourceStatusesToList(Map<Publisher, SourceStatus> sourceStatuses) {
        BasicDBList statuses = new BasicDBList();
        for (Entry<Publisher, SourceStatus> sourceStatus : sourceStatuses.entrySet()) {
            statuses.add(new BasicDBObject(ImmutableMap.of(
                    PUBLISHER_KEY, sourceStatus.getKey().key(),
                    STATE_KEY, sourceStatus.getValue().getState().toString().toLowerCase(),
                    "enabled", sourceStatus.getValue().isEnabled()
                    )));
        }
        return statuses;
    }

    public ApplicationSources fromDBObject(DBObject dbo) {

        List<Publisher> precedence = sourcePrecedenceFrom(dbo);
        List<DBObject> statusDbos = TranslatorUtils.toDBObjectList(dbo, SOURCES_KEY);
        Map<Publisher, SourceStatus> sourceStatuses = sourceStatusesFrom(statusDbos, precedence);

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

    private Map<Publisher, SourceStatus> sourceStatusesFrom(List<DBObject> list,
            List<Publisher> precedence) {
        Builder<Publisher, SourceStatus> builder = ImmutableMap.builder();
        for (DBObject dbo : list) {
            builder.put(
                    Publisher.fromKey(TranslatorUtils.toString(dbo, PUBLISHER_KEY)).requireValue(),
                    sourceStatusFrom(dbo)
                    );
        }
        Map<Publisher, SourceStatus> reads = builder.build();
        if (precedence != null) {
            // reorder if precedence enabled
            builder = ImmutableMap.builder();
            for (Publisher source : precedence) {
                builder.put(source,
                        reads.get(source));
            }
            reads = builder.build();
        }
        return reads;
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
