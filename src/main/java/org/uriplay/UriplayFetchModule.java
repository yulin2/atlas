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

package org.uriplay;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.uriplay.equiv.EquivModule;
import org.uriplay.media.entity.Content;
import org.uriplay.persistence.ContentPersistenceModule;
import org.uriplay.persistence.content.ContentWriter;
import org.uriplay.persistence.content.mongo.AliasWriter;
import org.uriplay.persistence.equiv.EquivalentContentFinder;
import org.uriplay.persistence.equiv.EquivalentContentMerger;
import org.uriplay.persistence.equiv.EquivalentContentMergingContentWriter;
import org.uriplay.persistence.equiv.EquivalentUrlFinder;
import org.uriplay.persistence.system.Fetcher;
import org.uriplay.query.uri.LocalOrRemoteFetcher;
import org.uriplay.query.uri.SavingFetcher;
import org.uriplay.query.uri.canonical.Canonicaliser;
import org.uriplay.query.uri.canonical.CanonicalisingFetcher;
import org.uriplay.remotesite.RemoteSiteModule;
import org.uriplay.remotesite.bbc.BbcUriCanonicaliser;
import org.uriplay.remotesite.bliptv.BlipTvAdapter;
import org.uriplay.remotesite.dailymotion.DailyMotionItemAdapter;
import org.uriplay.remotesite.hulu.HuluAllBrandsAdapter;
import org.uriplay.remotesite.hulu.HuluBrandAdapter;
import org.uriplay.remotesite.hulu.HuluItemAdapter;
import org.uriplay.remotesite.ted.TedTalkAdapter;
import org.uriplay.remotesite.tinyurl.ShortenedUrlCanonicaliser;
import org.uriplay.remotesite.youtube.YoutubeUriCanonicaliser;

import com.google.common.collect.Lists;

@Configuration
@Import({UriplayFetchModule.UriplayWriterModule.class, UriplayFetchModule.UriplayReaderModule.class})
public class UriplayFetchModule {
	
	
	@Configuration
	@Import({EquivModule.class})
	public static class UriplayWriterModule {
	
		private @Autowired EquivalentUrlFinder finder;
		private @Autowired ContentPersistenceModule persistence;
		private @Autowired RemoteSiteModule remote;
		private @Autowired UriplayReaderModule reader;
	
		public @Bean ContentWriter contentWriter() {		
			EquivalentContentMerger merger = new EquivalentContentMerger(new EquivalentContentFinder(finder, reader.contentResolverThatDoesntSave()));
			EquivalentContentMergingContentWriter writer = new EquivalentContentMergingContentWriter(persistence.persistentWriter(), merger);
			remote.contentWriters().add(writer);
			return writer;
		}
		
		public @PostConstruct void passWriterToReader() {
			reader.savingFetcher().setStore(contentWriter());
		}
	}
	
	@Configuration
	public static class UriplayReaderModule {
	
		private @Autowired ContentPersistenceModule persistence;
		
		private @Autowired AliasWriter aliasWriter;
		
		private @Autowired ShortenedUrlCanonicaliser shortUrlCanonicaliser;
		
		private @Autowired RemoteSiteModule remote;
	
		
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
			canonicalisers.add(new TedTalkAdapter.TedTalkCanonicaliser());
			canonicalisers.add(new DailyMotionItemAdapter.DailyMotionItemCanonicaliser());
			canonicalisers.add(new BlipTvAdapter.BlipTvCanonicaliser());
			canonicalisers.add(new HuluAllBrandsAdapter.HuluAllBrandsCanonicaliser());
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
