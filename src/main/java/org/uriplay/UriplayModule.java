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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.uriplay.equiv.EquivModule;
import org.uriplay.media.entity.Content;
import org.uriplay.persistence.UriplayPersistenceModule;
import org.uriplay.persistence.content.mongo.AliasWriter;
import org.uriplay.persistence.system.Fetcher;
import org.uriplay.query.QueryModule;
import org.uriplay.query.uri.LocalOrRemoteFetcher;
import org.uriplay.query.uri.SavingFetcher;
import org.uriplay.query.uri.canonical.Canonicaliser;
import org.uriplay.query.uri.canonical.CanonicalisingLocalRemoteFetcher;
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
@ImportResource("classpath:uriplay.xml")
@Import({EquivModule.class, QueryModule.class, RemoteSiteModule.class, UriplayPersistenceModule.class, UriplayWriterModule.class})
public class UriplayModule {
	
	private @Autowired UriplayPersistenceModule persistence;
	
	private @Autowired AliasWriter aliasWriter;
	
	private @Autowired ShortenedUrlCanonicaliser shortUrlCanonicaliser;
	
	private @Autowired RemoteSiteModule remote;

	private @Autowired UriplayWriterModule writer;
	
	public @Bean CanonicalisingLocalRemoteFetcher contentResolver() {
		Fetcher<Content> localOrRemoteFetcher = new LocalOrRemoteFetcher(persistence.mongoContentStore(), savingFetcher());
		
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
			
		return new CanonicalisingLocalRemoteFetcher(localOrRemoteFetcher, canonicalisers, aliasWriter);
	}
	
	public @Bean Fetcher<Content> savingFetcher() {
		return new SavingFetcher(remote.remoteFetcher(), writer.contentWriter());
	}
}
