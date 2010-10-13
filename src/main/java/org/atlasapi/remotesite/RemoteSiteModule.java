/* Copyright 2010 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.remotesite;

import java.util.List;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.system.Fetcher;
import org.atlasapi.remotesite.bbc.BbcModule;
import org.atlasapi.remotesite.bbc.BbcPodcastAdapter;
import org.atlasapi.remotesite.bbc.BbcProgrammeAdapter;
import org.atlasapi.remotesite.bliptv.BlipTvAdapter;
import org.atlasapi.remotesite.channel4.C4Module;
import org.atlasapi.remotesite.dailymotion.DailyMotionItemAdapter;
import org.atlasapi.remotesite.hulu.HuluAllBrandsAdapter;
import org.atlasapi.remotesite.hulu.HuluBrandAdapter;
import org.atlasapi.remotesite.hulu.HuluItemAdapter;
import org.atlasapi.remotesite.hulu.HuluRssAdapter;
import org.atlasapi.remotesite.ictomorrow.ICTomorrowModule;
import org.atlasapi.remotesite.imdb.ImdbAdapter;
import org.atlasapi.remotesite.itv.ItvBrandAdapter;
import org.atlasapi.remotesite.oembed.OembedXmlAdapter;
import org.atlasapi.remotesite.seesaw.SeesawBrandAdapter;
import org.atlasapi.remotesite.seesaw.SeesawItemAdapter;
import org.atlasapi.remotesite.synd.OpmlAdapter;
import org.atlasapi.remotesite.ted.TedTalkAdapter;
import org.atlasapi.remotesite.vimeo.VimeoAdapter;
import org.atlasapi.remotesite.youtube.YouTubeAdapter;
import org.atlasapi.remotesite.youtube.YouTubeFeedAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.google.common.collect.Lists;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.webapp.scheduling.ManualTaskTrigger;

@Configuration
@Import({C4Module.class, ICTomorrowModule.class, BbcModule.class})
public class RemoteSiteModule {

	private @Autowired AdapterLog log;
	private @Autowired C4Module c4Module; 
	
	private @Autowired BbcModule bbcModule; 
	
	public @Bean SimpleScheduler scheduler() {
	    return new SimpleScheduler();
	}
	
	public @Bean ManualTaskTrigger manualTaskTrigger() {
	    return new ManualTaskTrigger(scheduler());
	}
	
	public @Bean Fetcher<Content> remoteFetcher() {
		
		 PerSiteAdapterDispatcher dispatcher = new PerSiteAdapterDispatcher(log);
		 
		 List<SiteSpecificAdapter<? extends Content>> adapters = Lists.newArrayList();
		 
		 adapters.add(new YouTubeAdapter());
		 adapters.add(new YouTubeFeedAdapter());
		 // Commented out for now, as it generates too much gdata traffic
		 //adapters.add(new YouTubeUserAdapter());
		 adapters.add(new TedTalkAdapter());
		 
		 adapters.addAll(c4Module.adapters());
		 adapters.addAll(bbcModule.adapters());
		 
		 adapters.add(new DailyMotionItemAdapter());
		 adapters.add(new BlipTvAdapter());
		 adapters.add(new ItvBrandAdapter());
		 adapters.add(new BbcProgrammeAdapter(log));

		 adapters.add(new BbcPodcastAdapter());
		 
		 adapters.add(huluItemAdapter());
		 adapters.add(huluBrandAdapter());
		 adapters.add(huluAllBrandsAdapter());
		 
		 adapters.add(new HuluRssAdapter());
		 adapters.add(new VimeoAdapter());
		 
		 OembedXmlAdapter flickrAdapter = new OembedXmlAdapter();
		 flickrAdapter.setAcceptedUriPattern("http://www.flickr.com/photos/[^/]+/[\\d]+");
		 flickrAdapter.setOembedEndpoint("http://www.flickr.com/services/oembed/");
		 flickrAdapter.setPublisher(Publisher.FLICKR);
		 
		 adapters.add(flickrAdapter);
		 adapters.add(new OpmlAdapter(dispatcher));
		 
		 // avoid overloading with equiv requests
		 // adapters.add(new WikipediaSparqlAdapter());
		 
		 adapters.add(new ImdbAdapter(dispatcher));
		 
		 adapters.add(new SeesawBrandAdapter());
		 adapters.add(new SeesawItemAdapter());
		 
		 dispatcher.setAdapters(adapters);
		 return dispatcher;
	}
	
	public @Bean ContentWriters contentWriters() {
		return new ContentWriters();
	}
	
	public @Bean HuluItemAdapter huluItemAdapter() {
	    HuluItemAdapter huluItemAdapter = new HuluItemAdapter();
	    huluItemAdapter.setContentStore(contentWriters());
        huluItemAdapter.setBrandAdapter(huluBrandAdapter());
        return huluItemAdapter;
	}
	
	public @Bean HuluBrandAdapter huluBrandAdapter() {
	    HuluBrandAdapter huluBrandAdapter = new HuluBrandAdapter();
        huluBrandAdapter.setEpisodeAdapter(new HuluItemAdapter());
        return huluBrandAdapter;
	}
	
	public @Bean HuluAllBrandsAdapter huluAllBrandsAdapter() {
	    HuluAllBrandsAdapter allBrands = new HuluAllBrandsAdapter(huluBrandAdapter());
        allBrands.setContentStore(contentWriters());
        return allBrands;
	}
}
