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

package org.uriplay.remotesite;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.uriplay.media.entity.Content;
import org.uriplay.persistence.system.Fetcher;
import org.uriplay.remotesite.bbc.BbcIplayerFeedAdapter;
import org.uriplay.remotesite.bbc.BbcPodcastAdapter;
import org.uriplay.remotesite.bbc.BbcProgrammeAdapter;
import org.uriplay.remotesite.bliptv.BlipTvAdapter;
import org.uriplay.remotesite.channel4.C4AtomBackedBrandAdapter;
import org.uriplay.remotesite.channel4.C4BrandAtoZAdapter;
import org.uriplay.remotesite.channel4.C4HighlightsAdapter;
import org.uriplay.remotesite.dailymotion.DailyMotionItemAdapter;
import org.uriplay.remotesite.hulu.HuluAllBrandsAdapter;
import org.uriplay.remotesite.hulu.HuluBrandAdapter;
import org.uriplay.remotesite.hulu.HuluItemAdapter;
import org.uriplay.remotesite.hulu.HuluRssAdapter;
import org.uriplay.remotesite.imdb.ImdbAdapter;
import org.uriplay.remotesite.itv.ItvBrandAdapter;
import org.uriplay.remotesite.oembed.OembedXmlAdapter;
import org.uriplay.remotesite.synd.OpmlAdapter;
import org.uriplay.remotesite.ted.TedTalkAdapter;
import org.uriplay.remotesite.vimeo.VimeoAdapter;
import org.uriplay.remotesite.wikipedia.WikipediaSparqlAdapter;
import org.uriplay.remotesite.youtube.YouTubeAdapter;

import com.google.common.collect.Lists;

@Configuration
public class RemoteSiteModule {

	public @Bean Fetcher<Content> remoteFetcher() {
		
		 PerSiteAdapterDispatcher dispatcher = new PerSiteAdapterDispatcher();
		 
		 List<SiteSpecificAdapter<? extends Content>> adapters = Lists.newArrayList();
		 
		 adapters.add(new YouTubeAdapter());
		 adapters.add(new TedTalkAdapter());
		 adapters.add(new C4AtomBackedBrandAdapter());
		 adapters.add(new C4HighlightsAdapter());
		 adapters.add(new C4BrandAtoZAdapter());
		 adapters.add(new DailyMotionItemAdapter());
		 adapters.add(new BlipTvAdapter());
		 adapters.add(new ItvBrandAdapter());
		 adapters.add(new BbcIplayerFeedAdapter());
		 adapters.add(new BbcProgrammeAdapter());
		 adapters.add(new BbcPodcastAdapter());

		 HuluItemAdapter huluItemAdapter = new HuluItemAdapter();
		 
		 HuluBrandAdapter huluBrandAdapter = new HuluBrandAdapter();
		 huluBrandAdapter.setEpisodeAdapter(new HuluItemAdapter());

		 huluItemAdapter.setContentStore(contentWriters());
		 huluItemAdapter.setBrandAdapter(huluBrandAdapter);
		 
		 HuluAllBrandsAdapter allBrands = new HuluAllBrandsAdapter(huluBrandAdapter);
		 allBrands.setContentStore(contentWriters());
		 
		 adapters.add(huluItemAdapter);
		 adapters.add(huluBrandAdapter);
		 adapters.add(allBrands);
		 
		 adapters.add(new HuluRssAdapter());
		 adapters.add(new VimeoAdapter());

		 
		 OembedXmlAdapter flickrAdapter = new OembedXmlAdapter();
		 flickrAdapter.setAcceptedUriPattern("http://www.flickr.com/photos/[^/]+/[\\d]+");
		 flickrAdapter.setOembedEndpoint("http://www.flickr.com/services/oembed/");
		 flickrAdapter.setPublisher("flickr.com");
		 
		 adapters.add(flickrAdapter);
		 adapters.add(new OpmlAdapter(dispatcher));
		 adapters.add(new WikipediaSparqlAdapter());
		 adapters.add(new ImdbAdapter(dispatcher));
		 
		 dispatcher.setAdapters(adapters);
		 return dispatcher;
	}
	
	public ContentWriters contentWriters() {
		return new ContentWriters();
	}
}
