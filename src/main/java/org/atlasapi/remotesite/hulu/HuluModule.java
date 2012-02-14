package org.atlasapi.remotesite.hulu;

import javax.annotation.PostConstruct;

import org.atlasapi.persistence.logging.AdapterLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
@Import({HuluAdapterModule.class})
public class HuluModule {

	private @Autowired SimpleScheduler scheduler;
	private @Autowired HuluClient client;
	private @Autowired AdapterLog log;
	private @Autowired WritingHuluBrandAdapter huluBrandAdapter;
	
//	private LocalTime when = new LocalTime(2, 0, 0);
	
	@PostConstruct
	public void startTasks() {
	    scheduler.schedule(huluAllBrandsUpdater().withName("Hulu Updater"), RepetitionRules.NEVER);
	}
	
	public @Bean HuluAllBrandsUpdater huluAllBrandsUpdater() {
	    HuluAllBrandsUpdater allBrands = new HuluAllBrandsUpdater(client, huluBrandAdapter, log);
        return allBrands;
	}
}
