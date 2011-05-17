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

package org.atlasapi;

import java.util.List;

import javax.annotation.PostConstruct;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.persistence.ContentPersistenceModule;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.system.Fetcher;
import org.atlasapi.query.uri.LocalOrRemoteFetcher;
import org.atlasapi.query.uri.SavingFetcher;
import org.atlasapi.query.uri.canonical.Canonicaliser;
import org.atlasapi.query.uri.canonical.CanonicalisingFetcher;
import org.atlasapi.remotesite.RemoteSiteModule;
import org.atlasapi.remotesite.bbc.BbcUriCanonicaliser;
import org.atlasapi.remotesite.bliptv.BlipTvAdapter;
import org.atlasapi.remotesite.dailymotion.DailyMotionItemAdapter;
import org.atlasapi.remotesite.facebook.FacebookCanonicaliser;
import org.atlasapi.remotesite.hulu.HuluBrandAdapter;
import org.atlasapi.remotesite.hulu.HuluItemAdapter;
import org.atlasapi.remotesite.ted.TedTalkAdapter;
import org.atlasapi.remotesite.tinyurl.SavingShortUrlCanonicaliser;
import org.atlasapi.remotesite.tinyurl.ShortenedUrlCanonicaliser;
import org.atlasapi.remotesite.youtube.YouTubeFeedCanonicaliser;
import org.atlasapi.remotesite.youtube.YoutubeUriCanonicaliser;
import org.atlasapi.system.AliasController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import com.google.common.collect.Lists;
import com.metabroadcast.common.scheduling.SimpleScheduler;
import com.metabroadcast.common.webapp.scheduling.ManualTaskTrigger;

@Configuration
@Import({AtlasFetchModule.WriterModule.class, AtlasFetchModule.ReaderModule.class})
public class AtlasFetchModule {
	
	public @Bean SimpleScheduler scheduler() {
	    return new SimpleScheduler();
	}
	
	public @Bean ManualTaskTrigger manualTaskTrigger() {
	    return new ManualTaskTrigger(scheduler());
	}
	
	@Configuration
	public static class WriterModule {
	
		private @Autowired ContentPersistenceModule persistence;
		private @Autowired RemoteSiteModule remote;
		private @Autowired ReaderModule reader;
	
		@Primary
		public @Bean ContentWriter contentWriter() {		
			
			ContentWriter writer = persistence.persistentWriter();
			
			remote.contentWriters().add(writer);
			return writer;
		}
		
		public @PostConstruct void passWriterToReader() {
			reader.savingFetcher().setStore(contentWriter());
		}
		
		public @Bean AliasController aliasController() {
			return new AliasController(persistence.contentStore(), reader.contentResolverThatDoesntSave());
		}
	}
	
	@Configuration
	public static class ReaderModule {
	
		private @Autowired ContentPersistenceModule persistence;
		
		private @Autowired RemoteSiteModule remote;
		
		@Primary
		public @Bean CanonicalisingFetcher contentResolver() {
			Fetcher<Identified> localOrRemoteFetcher = new LocalOrRemoteFetcher(persistence.contentStore(), savingFetcher());
			return new CanonicalisingFetcher(localOrRemoteFetcher, canonicalisers());
		}
		
		public @Bean CanonicalisingFetcher contentResolverThatDoesntSave() {
			Fetcher<Identified> localOrRemoteFetcher = new LocalOrRemoteFetcher(persistence.contentStore(), remote.remoteFetcher());
			return new CanonicalisingFetcher(localOrRemoteFetcher, canonicalisers());
		}
		
		public @Bean List<Canonicaliser> canonicalisers() {
			List<Canonicaliser> canonicalisers = Lists.newArrayList();
			canonicalisers.add(new BbcUriCanonicaliser());
			canonicalisers.add(new YoutubeUriCanonicaliser());
			canonicalisers.add(new YouTubeFeedCanonicaliser());
			canonicalisers.add(new FacebookCanonicaliser());
			canonicalisers.add(new TedTalkAdapter.TedTalkCanonicaliser());
			canonicalisers.add(new DailyMotionItemAdapter.DailyMotionItemCanonicaliser());
			canonicalisers.add(new BlipTvAdapter.BlipTvCanonicaliser());
			canonicalisers.add(new HuluItemAdapter.HuluItemCanonicaliser());
			canonicalisers.add(new HuluBrandAdapter.HuluBrandCanonicaliser());
			canonicalisers.add(new SavingShortUrlCanonicaliser(new ShortenedUrlCanonicaliser(), persistence.shortUrlSaver()));
			return canonicalisers;
		}

		public @Bean SavingFetcher savingFetcher() {
			return new SavingFetcher(remote.remoteFetcher(), null);
		}
	}
}
