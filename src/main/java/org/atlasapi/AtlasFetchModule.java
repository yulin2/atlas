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

import org.atlasapi.equiv.EquivModule;
import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.ContentPersistenceModule;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.DefinitiveContentWriter;
import org.atlasapi.persistence.content.mongo.AliasWriter;
import org.atlasapi.persistence.equiv.EquivalentContentFinder;
import org.atlasapi.persistence.equiv.EquivalentContentMerger;
import org.atlasapi.persistence.equiv.EquivalentContentMergingContentWriter;
import org.atlasapi.persistence.equiv.EquivalentUrlFinder;
import org.atlasapi.persistence.system.Fetcher;
import org.atlasapi.query.uri.LocalOrRemoteFetcher;
import org.atlasapi.query.uri.SavingFetcher;
import org.atlasapi.query.uri.canonical.Canonicaliser;
import org.atlasapi.query.uri.canonical.CanonicalisingFetcher;
import org.atlasapi.remotesite.RemoteSiteModule;
import org.atlasapi.remotesite.bbc.BbcUriCanonicaliser;
import org.atlasapi.remotesite.bliptv.BlipTvAdapter;
import org.atlasapi.remotesite.dailymotion.DailyMotionItemAdapter;
import org.atlasapi.remotesite.hulu.HuluBrandAdapter;
import org.atlasapi.remotesite.hulu.HuluItemAdapter;
import org.atlasapi.remotesite.ted.TedTalkAdapter;
import org.atlasapi.remotesite.tinyurl.ShortenedUrlCanonicaliser;
import org.atlasapi.remotesite.youtube.YouTubeFeedCanonicaliser;
import org.atlasapi.remotesite.youtube.YoutubeUriCanonicaliser;
import org.atlasapi.remotesite.youtube.user.YouTubeUserCanonicaliser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import com.google.common.collect.Lists;

@Configuration
@Import({AtlasFetchModule.WriterModule.class, AtlasFetchModule.ReaderModule.class})
public class AtlasFetchModule {
	
	
	@Configuration
	@Import({EquivModule.class})
	public static class WriterModule {
	
		private @Value("${equivalence.enabled}") boolean enableEquivalence;
		
		private @Autowired EquivalentUrlFinder finder;
		private @Autowired ContentPersistenceModule persistence;
		private @Autowired RemoteSiteModule remote;
		private @Autowired ReaderModule reader;
	
		@Primary
		public @Bean ContentWriter contentWriter() {		
			
			ContentWriter writer = persistence.persistentWriter();
			DefinitiveContentWriter definitiveWriter = persistence.definitiveWriter();
			
			if (enableEquivalence) {
				EquivalentContentMerger merger = new EquivalentContentMerger(new EquivalentContentFinder(finder, reader.contentResolverThatDoesntSave()));
				writer = new EquivalentContentMergingContentWriter(writer, definitiveWriter, merger);
			}
			
			remote.contentWriters().add(writer);
			return writer;
		}
		
		public @PostConstruct void passWriterToReader() {
			reader.savingFetcher().setStore(contentWriter());
		}
	}
	
	@Configuration
	public static class ReaderModule {
	
		private @Autowired ContentPersistenceModule persistence;
		
		private @Autowired AliasWriter aliasWriter;
		
		private @Autowired ShortenedUrlCanonicaliser shortUrlCanonicaliser;
		
		private @Autowired RemoteSiteModule remote;
	
		@Primary
		public @Bean CanonicalisingFetcher contentResolver() {
			Fetcher<Content> localOrRemoteFetcher = new LocalOrRemoteFetcher(persistence.contentStore(), savingFetcher());
			
			return new CanonicalisingFetcher(localOrRemoteFetcher, canonicalisers(), aliasWriter);
		}
		
		public @Bean CanonicalisingFetcher contentResolverThatDoesntSave() {
			Fetcher<Content> localOrRemoteFetcher = new LocalOrRemoteFetcher(persistence.contentStore(), remote.remoteFetcher());
			return new CanonicalisingFetcher(localOrRemoteFetcher, canonicalisers(), aliasWriter);
		}
		

		private List<Canonicaliser> canonicalisers() {
			List<Canonicaliser> canonicalisers = Lists.newArrayList();
			canonicalisers.add(new BbcUriCanonicaliser());
			canonicalisers.add(new YoutubeUriCanonicaliser());
			canonicalisers.add(new YouTubeFeedCanonicaliser());
			canonicalisers.add(new YouTubeUserCanonicaliser());
			canonicalisers.add(new TedTalkAdapter.TedTalkCanonicaliser());
			canonicalisers.add(new DailyMotionItemAdapter.DailyMotionItemCanonicaliser());
			canonicalisers.add(new BlipTvAdapter.BlipTvCanonicaliser());
			canonicalisers.add(new HuluItemAdapter.HuluItemCanonicaliser());
			canonicalisers.add(new HuluBrandAdapter.HuluBrandCanonicaliser());
			canonicalisers.add(shortUrlCanonicaliser);
			return canonicalisers;
		}
		
		public @Bean SavingFetcher savingFetcher() {
			return new SavingFetcher(remote.remoteFetcher(), null);
		}
	}
}
