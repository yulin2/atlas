//package org.atlasapi.equiv;
//
//import javax.annotation.PostConstruct;
//
//import org.atlasapi.equiv.update.tasks.ScheduleTaskProgressStore;
//import org.atlasapi.media.entity.Publisher;
//import org.atlasapi.persistence.content.ContentResolver;
//import org.atlasapi.persistence.content.listing.ContentLister;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
//import com.metabroadcast.common.scheduling.RepetitionRules;
//import com.metabroadcast.common.scheduling.SimpleScheduler;
//
//@Configuration
//public class ChildRefUpdateModule {
//
//    private @Autowired ContentLister lister;
//    private @Autowired ContentResolver resolver;
//    private @Autowired DatabasedMongo mongo;
//    private @Autowired ScheduleTaskProgressStore progressStore;
//    private @Autowired SimpleScheduler scheduler;
//
//    @PostConstruct
//    public void setup() {
//        scheduler.schedule(childRefUpdateTask(), RepetitionRules.NEVER);
//    }
//    
//    @Bean
//    public ChildRefUpdateController childRefUpdateTaskController() {
//        return new ChildRefUpdateController(childRefUpdateTask(), resolver);
//    }
//    
//    @Bean
//    public ChildRefUpdateTask childRefUpdateTask() {
//        return new ChildRefUpdateTask(lister, resolver, mongo, progressStore).forPublishers(Publisher.all().toArray(new Publisher[]{}));
//    }
//    
//}
