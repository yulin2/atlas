package org.atlasapi.equiv;

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
        int processed = 0;
        int equivs = 0;
        do {
            contents = contentStore.iterate(queryFor(clock.now()), lastId, BATCH_SIZE);
            for (Brand brand : Iterables.filter(contents, Brand.class)) {
                try {
                    List<Brand> updatedBrands = new BrandEquivUpdateTask(brand, scheduleResolver).call();
                    if(!updatedBrands.isEmpty()) {
                        System.out.print(brand.getTitle() + " : ");
                        equivs++;
                        for (Brand equiv : Iterables.skip(updatedBrands,1)) {
                            System.out.println(equiv.getTitle() + ", ");
                        }
                        System.out.println("<<<<----------------");
                    }
                } catch (Exception e) {
                    log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Exception updating equivalence for "+brand.getCanonicalUri()));
                }
                lastId = brand.getCanonicalUri();
                processed += contents.size();
                System.out.println(processed);
            }
        } while ((contents.size() == BATCH_SIZE));
        System.out.println("Brands with equivs" + equivs);
    }
    
    private MongoQueryBuilder queryFor(DateTime now) {
        return where().fieldEquals("publisher", Publisher.PA.key()).fieldAfter("lastFetched", now.minus(Duration.standardDays(1)));
    }

}
