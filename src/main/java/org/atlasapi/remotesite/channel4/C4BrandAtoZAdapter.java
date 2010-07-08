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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.SiteSpecificAdapter;

import com.metabroadcast.common.base.Maybe;

/**
 * {@link SiteSpecificRepresentationAdapter} for screen-scraping from Channel4's 4OD website
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class C4BrandAtoZAdapter extends BaseC4PlaylistClient implements SiteSpecificAdapter<Playlist> {

	private static final String C4_ATOZ_CURIE_PREFIX = "c4:atoz_";
	private static final String C4_ATOZ_URI_PREFIX = "http://www.channel4.com/programmes/atoz/";
	private static final Pattern ATOZ = Pattern.compile(C4_ATOZ_URI_PREFIX + "([a-z]|0-9)");

	public C4BrandAtoZAdapter() {
		this(new C4BrandAtoZClient(), new C4AtomBackedBrandAdapter());
	}
	
	public C4BrandAtoZAdapter(RemoteSiteClient<BrandListingPage> brandListClient, SiteSpecificAdapter<Brand> brandClient) {
		super(brandListClient, brandClient);
	}

	public boolean canFetch(String uri) {
		return ATOZ.matcher(uri).matches();
	}

	public static Maybe<String> compact(String uri) {
		Matcher matcher = ATOZ.matcher(uri);
		if (matcher.matches()) {
			return Maybe.just(C4_ATOZ_CURIE_PREFIX +  matcher.group(1));
		}
		return Maybe.nothing();
	}

	public static Maybe<String> expand(String curie) {
		if (curie.startsWith(C4_ATOZ_CURIE_PREFIX)) {
			return Maybe.just(C4_ATOZ_URI_PREFIX + curie.substring(C4_ATOZ_CURIE_PREFIX.length()));
		}
		return Maybe.nothing();
	}
}
