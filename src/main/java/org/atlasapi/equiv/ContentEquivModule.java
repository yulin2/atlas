package org.atlasapi.equiv;

import static org.atlasapi.equiv.results.EquivalenceResultBuilder.resultBuilder;
import static org.atlasapi.equiv.update.ResultWritingEquivalenceUpdater.resultWriter;

import java.util.Set;

import javax.annotation.PostConstruct;

import org.atlasapi.equiv.generators.BroadcastMatchingItemEquivalenceGenerator;
import org.atlasapi.equiv.generators.ContentEquivalenceGenerator;
import org.atlasapi.equiv.generators.ItemBasedContainerEquivalenceGenerator;
import org.atlasapi.equiv.results.combining.AddingEquivalenceCombiner;
import org.atlasapi.equiv.results.extractors.TopEquivalenceExtractor;
import org.atlasapi.equiv.results.persistence.EquivalenceResultStore;
import org.atlasapi.equiv.results.persistence.MongoEquivalenceResultStore;
import org.atlasapi.equiv.update.BasicEquivalenceUpdater;
import org.atlasapi.equiv.update.ContentEquivalenceUpdateController;
import org.atlasapi.equiv.update.ContentEquivalenceUpdateTask;
import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.equiv.update.ResultWritingEquivalenceUpdater;
import org.atlasapi.equiv.update.RootEquivalenceUpdater;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.RetrospectiveContentLister;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.scheduling.RepetitionRule;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.time.DayOfWeek;

@Configuration
public class ContentEquivModule {
    
    private static final RepetitionRule EQUIVALENCE_REPETITION = RepetitionRules.weekly(DayOfWeek.MONDAY, new LocalTime(9, 00));
    
    private @Autowired DatabasedMongo mongo;
    private @Autowired ScheduleResolver scheduleResolver;
    private @Autowired RetrospectiveContentLister contentLister;
    private @Autowired ContentResolver contentResolver;
    private @Autowired AdapterLog log;
    private @Autowired SimpleScheduler taskScheduler;

    public @Bean EquivalenceResultStore equivalenceResultStore() {
        return new MongoEquivalenceResultStore(mongo);
    }
    
    public @Bean ContentEquivalenceUpdater<Item> itemUpdater() {
        Set<ContentEquivalenceGenerator<Item>> calculators = ImmutableSet.<ContentEquivalenceGenerator<Item>>of(
                new BroadcastMatchingItemEquivalenceGenerator(scheduleResolver, ImmutableSet.copyOf(Publisher.values()), Duration.standardMinutes(1))
        );
        return new ResultWritingEquivalenceUpdater<Item>(new BasicEquivalenceUpdater<Item>(calculators, resultBuilder(AddingEquivalenceCombiner.<Item>create(), TopEquivalenceExtractor.<Item>create())), equivalenceResultStore());
    }
    
    public @Bean ContentEquivalenceUpdater<Container<?>> containerUpdater() {
        Set<ContentEquivalenceGenerator<Container<?>>> calculators = ImmutableSet.<ContentEquivalenceGenerator<Container<?>>>of(
                new ItemBasedContainerEquivalenceGenerator(itemUpdater())
        );
        return resultWriter(new BasicEquivalenceUpdater<Container<?>>(calculators , resultBuilder(AddingEquivalenceCombiner.<Container<?>>create(), TopEquivalenceExtractor.<Container<?>>create())), equivalenceResultStore());
    }

    private @Bean RootEquivalenceUpdater contentUpdater() {
        return new RootEquivalenceUpdater(containerUpdater(), itemUpdater());
    }
    
    public @Bean ContentEquivalenceUpdateTask updateTask() {
        return new ContentEquivalenceUpdateTask(contentLister, contentUpdater(), log);
    }
    
    @PostConstruct
    public void scheduleUpdater() {
        taskScheduler.schedule(updateTask(), EQUIVALENCE_REPETITION);
    }
    
    public @Bean ContentEquivalenceUpdateController updateController() {
        return new ContentEquivalenceUpdateController(contentUpdater(), contentResolver);
    }
}
