package org.atlasapi.remotesite.hulu;

import javax.annotation.PostConstruct;

import org.atlasapi.remotesite.ContentWriters;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
@Import({HuluAdapterModule.class})
public class HuluModule {

	private @Autowired ContentWriters writers;
	private @Autowired SimpleScheduler scheduler;
	
	private @Autowired HuluBrandAdapter huluBrandAdapter;
	
	private LocalTime when = new LocalTime(2, 0, 0);
	
	@PostConstruct
	public void startTasks() {
	    scheduler.schedule(huluAllBrandsUpdater(), RepetitionRules.daily(when));
	}
	
	public @Bean HuluAllBrandsUpdater huluAllBrandsUpdater() {
	    HuluAllBrandsUpdater allBrands = new HuluAllBrandsUpdater(huluBrandAdapter);
        allBrands.setContentStore(writers);
        return allBrands;
	}
}
