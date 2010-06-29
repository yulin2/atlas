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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uriplay.media.entity.Brand;
import org.uriplay.persistence.system.RemoteSiteClient;
import org.uriplay.remotesite.ContentExtractor;
import org.uriplay.remotesite.SiteSpecificAdapter;
import org.uriplay.remotesite.http.CommonsHttpClient;

import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.WireFeedInput;

public class C4AtomBackedBrandAdapter implements SiteSpecificAdapter<Brand> {

	private static final Pattern BRAND_PAGE_PATTERN = Pattern.compile("(http://www.channel4.com/programmes/[^/\\s]+)(/4od)?");

	private final Log log = LogFactory.getLog(getClass());
	
	private final RemoteSiteClient<Feed> feedClient;
	private final ContentExtractor<Feed, Brand> extractor;
	
	public C4AtomBackedBrandAdapter() {
		this(atomClient(), new C4BrandExtractor());
	}
	
	public C4AtomBackedBrandAdapter(RemoteSiteClient<Feed> feedClient, ContentExtractor<Feed, Brand> extractor) {
		this.feedClient = feedClient;
		this.extractor = extractor;
	}
	
	@Override
	public boolean canFetch(String uri) {
		return BRAND_PAGE_PATTERN.matcher(uri).matches();
	}

	@Override
	public Brand fetch(String uri) {
		try {
			log.info("Fetching C4 brand " + uri);
			return extractor.extract(feedClient.get(atomUrl(uri)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String atomUrl(String uri) {
		Matcher matcher = BRAND_PAGE_PATTERN.matcher(uri);
		if (!matcher.matches()) {
			throw new IllegalArgumentException();
		}
		String programmeUri = matcher.group(1);
		return programmeUri + "/4od.atom";
	}
	
	private static RemoteSiteClient<Feed> atomClient() {
		
		return new RemoteSiteClient<Feed>() {
			
			private final CommonsHttpClient client = new CommonsHttpClient();

			@Override
			public Feed get(String uri) throws Exception {
				return (Feed) new WireFeedInput().build(client.get(uri));
			}
		};
	}

}
