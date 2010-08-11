/* Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.remotesite.channel4;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.SiteSpecificAdapter;

import com.metabroadcast.common.base.Maybe;

/**
 * {@link SiteSpecificAdapter} for screen-scraping from Channel4's 4OD website
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class C4HighlightsAdapter extends BaseC4PlaylistClient {

	private static final String C4_HIGHLIGHTS_CURIE = "c4:highlights";
	private static final String C4_MOST_POPULAR_CURIE = "c4:most-popular";
	
	private static final String HIGHLIGHTS_URI = "http://www.channel4.com/programmes/4od/highlights";
	private static final String CURRENT_MOST_POPULAR_URI = "http://www.channel4.com/programmes/4od/most-popular";
	
	public C4HighlightsAdapter(C4AtomBackedBrandAdapter brandAdapter) {
		this(new C4HomePageClient(), brandAdapter);
	}
	
	public C4HighlightsAdapter(RemoteSiteClient<BrandListingPage> brandListClient, SiteSpecificAdapter<Brand> brandClient) {
		super(brandListClient, brandClient);
	}

	public boolean canFetch(String uri) {
		return CURRENT_MOST_POPULAR_URI.equals(uri) || HIGHLIGHTS_URI.equals(uri);
	}

	public static Maybe<String> compact(String uri) {
		if (CURRENT_MOST_POPULAR_URI.equals(uri)) {
			return Maybe.just(C4_MOST_POPULAR_CURIE);
		}
		if (HIGHLIGHTS_URI.equals(uri)) {
			return Maybe.just(C4_HIGHLIGHTS_CURIE);
		}
		return Maybe.nothing();
	}

	public static Maybe<String> expand(String curie) {
		if (C4_HIGHLIGHTS_CURIE.equals(curie)) {
			return Maybe.just(HIGHLIGHTS_URI);
		}
		if (C4_MOST_POPULAR_CURIE.equals(curie)) {
			return Maybe.just(CURRENT_MOST_POPULAR_URI);
		}
		return Maybe.nothing();
	}
}
