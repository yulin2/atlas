package org.atlasapi.remotesite.hulu;

import java.util.List;

import javax.annotation.PostConstruct;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.remotesite.ContentWriters;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.scheduling.RepetitionRules;
import com.metabroadcast.common.scheduling.SimpleScheduler;

@Configuration
public class HuluModule {

	private @Autowired ContentWriters writers;
	private @Autowired SimpleScheduler scheduler;
	private @Value("${hulu.enabled}") String enabled;
	
	private LocalTime when = new LocalTime(2, 0, 0);
	
	@PostConstruct
	public void startTasks() {
	    if (Boolean.parseBoolean(enabled)) {
	        scheduler.schedule(huluAllBrandsUpdater(), RepetitionRules.daily(when));
	    }
	}
	
	public List<SiteSpecificAdapter<? extends Identified>> adapters() {
		return ImmutableList.<SiteSpecificAdapter<? extends Identified>>of(huluItemAdapter(), huluBrandAdapter(), new HuluRssAdapter());
	}
	
	public @Bean HuluItemAdapter huluItemAdapter() {
	    HuluItemAdapter huluItemAdapter = new HuluItemAdapter();
	    huluItemAdapter.setContentStore(writers);
        huluItemAdapter.setBrandAdapter(huluBrandAdapter());
        return huluItemAdapter;
	}
	
	public @Bean HuluBrandAdapter huluBrandAdapter() {
	    HuluBrandAdapter huluBrandAdapter = new HuluBrandAdapter();
        huluBrandAdapter.setEpisodeAdapter(new HuluItemAdapter());
        return huluBrandAdapter;
	}
	
	public @Bean HuluAllBrandsUpdater huluAllBrandsUpdater() {
	    HuluAllBrandsUpdater allBrands = new HuluAllBrandsUpdater(huluBrandAdapter());
        allBrands.setContentStore(writers);
        return allBrands;
	}
}
