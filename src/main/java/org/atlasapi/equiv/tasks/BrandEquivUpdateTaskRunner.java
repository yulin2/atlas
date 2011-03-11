package org.atlasapi.equiv.tasks;

import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;

import java.util.List;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.MongoQueryBuilder;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.SystemClock;

public class BrandEquivUpdateTaskRunner implements Runnable {

    private static final int BATCH_SIZE = 10;
    private final Clock clock;
    private final MongoDbBackedContentStore contentStore;
    private final ScheduleResolver scheduleResolver;
    private final AdapterLog log;

    public BrandEquivUpdateTaskRunner(MongoDbBackedContentStore contentStore, ScheduleResolver scheduleResolver, AdapterLog log) {
        this(contentStore, scheduleResolver, log, new SystemClock());
    }
    
    public BrandEquivUpdateTaskRunner(MongoDbBackedContentStore contentStore, ScheduleResolver scheduleResolver, AdapterLog log, Clock clock) {
        this.contentStore = contentStore;
        this.scheduleResolver = scheduleResolver;
        this.log = log;
        this.clock = clock;
    }
    
    @Override
    public void run() {
        String lastId = null;
        List<Content> contents;
        do {
            contents = contentStore.iterate(queryFor(clock.now()), lastId, BATCH_SIZE);
            for (Brand brand : Iterables.filter(contents, Brand.class)) {
                try {
                    System.out.println(new BrandEquivUpdateTask(brand, scheduleResolver, contentStore, log).writesResults(false).call());
                } catch (Exception e) {
                    log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Exception updating equivalence for "+brand.getCanonicalUri()));
                }
                lastId = brand.getCanonicalUri();
            }
        } while ((contents.size() == BATCH_SIZE));
    }
    
    private MongoQueryBuilder queryFor(DateTime now) {
        return where().fieldEquals("publisher", Publisher.PA.key()).fieldAfter("lastFetched", now.minus(Duration.standardDays(1)));
    }

}
