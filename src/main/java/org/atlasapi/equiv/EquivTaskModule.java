package org.atlasapi.equiv;

import static org.atlasapi.media.entity.Publisher.BBC;
import static org.atlasapi.media.entity.Publisher.BBC_REDUX;
import static org.atlasapi.media.entity.Publisher.C4;
import static org.atlasapi.media.entity.Publisher.FIVE;
import static org.atlasapi.media.entity.Publisher.ITUNES;
import static org.atlasapi.media.entity.Publisher.ITV;
import static org.atlasapi.media.entity.Publisher.LOVEFILM;
import static org.atlasapi.media.entity.Publisher.PA;
import static org.atlasapi.media.entity.Publisher.RADIO_TIMES;

import java.util.Set;

import javax.annotation.PostConstruct;

import org.atlasapi.equiv.results.persistence.RecentEquivalenceResultStore;
import org.atlasapi.equiv.results.probe.EquivalenceProbeStore;
import org.atlasapi.equiv.results.probe.EquivalenceResultProbeController;
import org.atlasapi.equiv.results.probe.MongoEquivalenceProbeStore;
import org.atlasapi.equiv.results.www.EquivalenceResultController;
import org.atlasapi.equiv.results.www.RecentResultController;
import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.equiv.update.tasks.ContentEquivalenceUpdateTask;
import org.atlasapi.equiv.update.tasks.MongoScheduleTaskProgressStore;
import org.atlasapi.equiv.update.www.ContentEquivalenceUpdateController;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.logging.AdapterLog;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.scheduling.RepetitionRule;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
@Import(EquivModule.class)
public class EquivTaskModule {

    private static final Set<String> ignored = ImmutableSet.of("http://www.bbc.co.uk/programmes/b006mgyl"); 
    private static final RepetitionRule EQUIVALENCE_REPETITION = RepetitionRules.daily(new LocalTime(9, 00));
    
    private @Value("${equiv.updater.enabled}") String updaterEnabled;
    
    private @Autowired ContentLister contentLister;
    private @Autowired SimpleScheduler taskScheduler;
    private @Autowired ContentResolver contentResolver;
    private @Autowired DatabasedMongo db;
    private @Autowired AdapterLog log;
    
    private @Autowired @Qualifier("contentUpdater") ContentEquivalenceUpdater<Content> contentUpdater;
    private @Autowired RecentEquivalenceResultStore equivalenceResultStore;
    
    
    @PostConstruct
    public void scheduleUpdater() {
        if(Boolean.parseBoolean(updaterEnabled)) {
            taskScheduler.schedule(publisherUpdateTask(BBC, C4, ITV, FIVE, RADIO_TIMES, BBC_REDUX).withName("BBC/C4/ITV/Five/RT/Redux Equivalence Updater"), EQUIVALENCE_REPETITION);
            taskScheduler.schedule(publisherUpdateTask(PA).withName("PA Equivalence Updater"), RepetitionRules.NEVER);
            taskScheduler.schedule(publisherUpdateTask(BBC).withName("BBC Equivalence Updater"), RepetitionRules.NEVER);
            taskScheduler.schedule(publisherUpdateTask(C4).withName("C4 Equivalence Updater"), RepetitionRules.NEVER);
            taskScheduler.schedule(publisherUpdateTask(ITV).withName("ITV Equivalence Updater"), RepetitionRules.NEVER);
            taskScheduler.schedule(publisherUpdateTask(FIVE).withName("Five Equivalence Updater"), RepetitionRules.NEVER);
            taskScheduler.schedule(publisherUpdateTask(BBC_REDUX).withName("Redux Equivalence Updater"), RepetitionRules.NEVER);
            taskScheduler.schedule(publisherUpdateTask(ITUNES).withName("Itunes Equivalence Updater"), RepetitionRules.NEVER);
            taskScheduler.schedule(publisherUpdateTask(RADIO_TIMES).withName("RT Equivalence Updater"), RepetitionRules.NEVER);
            taskScheduler.schedule(publisherUpdateTask(LOVEFILM).withName("Lovefilm Equivalence Updater"), RepetitionRules.NEVER);
            
            taskScheduler.schedule(publisherUpdateTask(Publisher.BBC_MUSIC).withName("Music Equivalence Updater"), RepetitionRules.NEVER);
            //taskScheduler.schedule(childRefUpdateTask().forPublishers(Publisher.BBC).withName("BBC Child Ref Update"), RepetitionRules.NEVER);
            //taskScheduler.schedule(childRefUpdateTask().forPublishers(Publisher.PA).withName("PA Child Ref Update"), RepetitionRules.NEVER);
            //taskScheduler.schedule(childRefUpdateTask().forPublishers(publishersApartFrom(Publisher.BBC, Publisher.PA)).withName("Other Publishers Child Ref Update"), RepetitionRules.NEVER);
        }
    }
    
    public @Bean MongoScheduleTaskProgressStore progressStore() {
        return new MongoScheduleTaskProgressStore(db);
    }
    
    private ContentEquivalenceUpdateTask publisherUpdateTask(final Publisher... publishers) {
        return new ContentEquivalenceUpdateTask(contentLister, contentUpdater, log, progressStore(), ignored).forPublishers(publishers);
    }
    
    /*private Publisher[] publishersApartFrom(Publisher...publishers) {
        SetView<Publisher> remainingPublishers = Sets.difference(ImmutableSet.copyOf(Publisher.values()), ImmutableSet.copyOf(publishers));
        return remainingPublishers.toArray(new Publisher[remainingPublishers.size()]);
    }*/

    //Controllers...
    public @Bean ContentEquivalenceUpdateController contentEquivalenceUpdateController() {
        return new ContentEquivalenceUpdateController(contentUpdater, contentResolver, log);
    }
    
    public @Bean EquivalenceResultController resultEquivalenceResultController() {
        return new EquivalenceResultController(equivalenceResultStore, equivProbeStore(), contentResolver);
    }
    
    public @Bean RecentResultController recentEquivalenceResultController() {
        return new RecentResultController(equivalenceResultStore);
    }
    
    //Probes...
    public @Bean EquivalenceProbeStore equivProbeStore() { 
        return new MongoEquivalenceProbeStore(db);
    }
    
    public @Bean EquivalenceResultProbeController equivProbeController() {
        return new EquivalenceResultProbeController(equivalenceResultStore, equivProbeStore());
    }

//    @Bean ManualScheduleUpdateController scheduleUpdateController() {
//        return new ManualScheduleUpdateController(scheduleResolver, contentResolver, channelResolver);
//    }
    
    
}
