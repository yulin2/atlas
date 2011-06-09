package org.atlasapi.equiv.tasks;

import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;

import java.util.List;

import org.atlasapi.equiv.tasks.persistence.EquivResultStore;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.MongoQueryBuilder;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.common.time.SystemClock;

public class PaEquivUpdateTask implements Runnable {

    private static final int BATCH_SIZE = 10;
    private final Clock clock;
    private final MongoDbBackedContentStore contentStore;
    private final AdapterLog log;
    private final ItemBasedBrandEquivUpdater brandUpdater;
    private final EquivResultStore equivResultStore;
    private final FilmEquivUpdater filmUpdater;

    public PaEquivUpdateTask(MongoDbBackedContentStore contentStore, ItemBasedBrandEquivUpdater brandUpdater, FilmEquivUpdater filmUpdater, EquivResultStore equivResultStore, AdapterLog log) {
        this(contentStore, brandUpdater, filmUpdater, equivResultStore, log, new SystemClock());
    }
    
    public PaEquivUpdateTask(MongoDbBackedContentStore contentStore, ItemBasedBrandEquivUpdater brandUpdater, FilmEquivUpdater filmUpdater, EquivResultStore equivResultStore, AdapterLog log, Clock clock) {
        this.contentStore = contentStore;
        this.filmUpdater = filmUpdater;
        this.equivResultStore = equivResultStore;
        this.log = log;
        this.clock = clock;
        this.brandUpdater = brandUpdater;
    }
    
    @Override
    public void run() {
        log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription("Starting equivalence task"));
        DateTime start = new DateTime(DateTimeZones.UTC);
        int processed = 0;
        
        String lastId = null;
        List<Content> contents;
        do {
            contents = contentStore.iterateOverContent(queryFor(clock.now()), lastId, -BATCH_SIZE);
            for (Brand brand : Iterables.filter(contents, Brand.class)) {
                processed++;
                try {
                    equivResultStore.store(brandUpdater.updateEquivalence(brand));
                } catch (Exception e) {
                    log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Exception updating equivalence for "+brand.getCanonicalUri()));
                }
            }
            
            for (Film film : Iterables.filter(contents, Film.class)) {
                processed++;
                try {
                    filmUpdater.updateEquivalence(film);
                } catch (Exception e) {
                    log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Exception updating equivalence for "+film.getCanonicalUri()));
                }
                
            }
            
            lastId = contents.isEmpty() ? lastId : Iterables.getLast(contents).getCanonicalUri();
        } while (!contents.isEmpty());
        
        String runTime = new Period(start, new DateTime(DateTimeZones.UTC)).toString(PeriodFormat.getDefault());
        log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription(String.format("Finish equivalence task in %s. %s brands processed", runTime, processed)));
    }
    
    private MongoQueryBuilder queryFor(DateTime now) {
        return where().fieldEquals("publisher", Publisher.PA.key())/*.fieldAfter("lastFetched", now.minus(Duration.standardDays(1)))*/;
    }
}
