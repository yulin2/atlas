package org.atlasapi.remotesite.redux;

import static org.atlasapi.remotesite.redux.HttpBackedReduxClient.reduxClientForHost;

import java.text.ParseException;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.net.HostSpecifier;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.security.UsernameAndPassword;

@Configuration
public class ReduxModule {

    private @Autowired ContentWriter writer;
    private @Autowired AdapterLog log;
    private @Autowired SimpleScheduler taskScheduler;

    private @Value("${redux.host}") String reduxHost;
    private @Value("${redux.username}") String reduxUsername;
    private @Value("${redux.password}") String reduxPassword;
    
    
    protected @Bean ReduxClient reduxClient() {
        try {
            return reduxClientForHost(HostSpecifier.from(reduxHost))
                    .withCredentials(new UsernameAndPassword(reduxUsername, reduxPassword))
                    .withLog(log).withMaxRequestsPerSecond(5).build();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    
    @PostConstruct
    public void scheduleTasks() {
        taskScheduler.schedule(new ReduxLatestUpdateTasks.MaximumReduxLatestUpdateTask(1000, reduxClient(), reduxProgrammeHandler(), log).withName("Redux Latest 1000 updater"), RepetitionRules.NEVER);
        taskScheduler.schedule(new ReduxLatestUpdateTasks.FullReduxLatestUpdateTask(reduxClient(), reduxProgrammeHandler(), log).withName("Redux Complete Latest updater"), RepetitionRules.NEVER);
    }
    
    public @Bean ReduxUpdateController updateController() {
        return new ReduxUpdateController(reduxClient(), reduxProgrammeHandler(), log);
    }

    protected @Bean ReduxProgrammeHandler reduxProgrammeHandler() {
        return new DefaultReduxProgrammeHandler(writer, new FullProgrammeItemExtractor(log));
    }
    
}
