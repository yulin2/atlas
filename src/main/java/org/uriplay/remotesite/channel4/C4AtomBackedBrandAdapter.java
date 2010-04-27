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


package org.uriplay.remotesite.channel4;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jherd.remotesite.SiteSpecificAdapter;
import org.jherd.remotesite.http.CommonsHttpClient;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.jherd.remotesite.timing.RequestTimer;
import org.uriplay.media.entity.Brand;
import org.uriplay.remotesite.ContentExtractor;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;

public class C4AtomBackedBrandAdapter implements SiteSpecificAdapter<Brand> {

	private static final Pattern BRAND_PAGE_PATTERN = Pattern.compile("http://www.channel4.com/programmes/[^/\\s]+/4od");

	private final Log log = LogFactory.getLog(getClass());
	
	private final RemoteSiteClient<SyndFeed> feedClient;
	private final ContentExtractor<SyndFeed, Brand> extractor;
	
	public C4AtomBackedBrandAdapter() {
		this(atomClient(), new C4BrandExtractor());
	}
	
	public C4AtomBackedBrandAdapter(RemoteSiteClient<SyndFeed> feedClient, ContentExtractor<SyndFeed, Brand> extractor) {
		this.feedClient = feedClient;
		this.extractor = extractor;
	}
	
	@Override
	public boolean canFetch(String uri) {
		return BRAND_PAGE_PATTERN.matcher(uri).matches();
	}

	@Override
	public Brand fetch(String uri, RequestTimer timer) {
		try {
			log.info("Fetching C4 brand " + uri);
			return extractor.extract(feedClient.get(uri + ".atom"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static RemoteSiteClient<SyndFeed> atomClient() {
		
		return new RemoteSiteClient<SyndFeed>() {
			
			private final CommonsHttpClient client = new CommonsHttpClient();

			@Override
			public SyndFeed get(String uri) throws Exception {
				return new SyndFeedInput().build(client.get(uri));
			}
		};
	}

}
