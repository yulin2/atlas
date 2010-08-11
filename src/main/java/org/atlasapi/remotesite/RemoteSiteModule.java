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
import org.atlasapi.persistence.system.Fetcher;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.bbc.BbcIplayerFeedAdapter;
import org.atlasapi.remotesite.bbc.BbcPodcastAdapter;
import org.atlasapi.remotesite.bbc.BbcProgrammeAdapter;
import org.atlasapi.remotesite.bliptv.BlipTvAdapter;
import org.atlasapi.remotesite.channel4.ApiKeyAwareClient;
import org.atlasapi.remotesite.channel4.C4AtomBackedBrandAdapter;
import org.atlasapi.remotesite.channel4.C4BrandAtoZAdapter;
import org.atlasapi.remotesite.channel4.C4HighlightsAdapter;
import org.atlasapi.remotesite.channel4.RequestLimitingRemoteSiteClient;
import org.atlasapi.remotesite.dailymotion.DailyMotionItemAdapter;
import org.atlasapi.remotesite.hulu.HuluAllBrandsAdapter;
import org.atlasapi.remotesite.hulu.HuluBrandAdapter;
import org.atlasapi.remotesite.hulu.HuluItemAdapter;
import org.atlasapi.remotesite.hulu.HuluRssAdapter;
import org.atlasapi.remotesite.imdb.ImdbAdapter;
import org.atlasapi.remotesite.itv.ItvBrandAdapter;
import org.atlasapi.remotesite.oembed.OembedXmlAdapter;
import org.atlasapi.remotesite.support.atom.AtomClient;
import org.atlasapi.remotesite.synd.OpmlAdapter;
import org.atlasapi.remotesite.ted.TedTalkAdapter;
import org.atlasapi.remotesite.vimeo.VimeoAdapter;
import org.atlasapi.remotesite.wikipedia.WikipediaSparqlAdapter;
import org.atlasapi.remotesite.youtube.YouTubeAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Lists;
import com.sun.syndication.feed.atom.Feed;

@Configuration
public class RemoteSiteModule {

	private @Value("${c4.apiKey}") String c4ApiKey;
	
	public @Bean Fetcher<Content> remoteFetcher() {
		
		 PerSiteAdapterDispatcher dispatcher = new PerSiteAdapterDispatcher();
		 
		 List<SiteSpecificAdapter<? extends Content>> adapters = Lists.newArrayList();
		 
		 adapters.add(new YouTubeAdapter());
		 adapters.add(new TedTalkAdapter());
		 
		 RemoteSiteClient<Feed> c4AtomFetcher = new RequestLimitingRemoteSiteClient<Feed>(new ApiKeyAwareClient<Feed>(c4ApiKey, new AtomClient()), 4);
		 C4AtomBackedBrandAdapter brandFetcher = new C4AtomBackedBrandAdapter(c4AtomFetcher);
		
		 adapters.add(brandFetcher);
		 
		 adapters.add(new C4HighlightsAdapter(brandFetcher));
		 adapters.add(new C4BrandAtoZAdapter(brandFetcher));
		 adapters.add(new DailyMotionItemAdapter());
		 adapters.add(new BlipTvAdapter());
		 adapters.add(new ItvBrandAdapter());
		 adapters.add(new BbcIplayerFeedAdapter());
		 adapters.add(new BbcProgrammeAdapter());
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
		 adapters.add(new WikipediaSparqlAdapter());
		 adapters.add(new ImdbAdapter(dispatcher));
		 
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
