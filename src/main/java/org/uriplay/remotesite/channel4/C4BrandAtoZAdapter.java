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

package org.uriplay.remotesite.channel4;

import javax.xml.bind.JAXBException;

import org.jherd.remotesite.SiteSpecificAdapter;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Playlist;

/**
 * {@link SiteSpecificRepresentationAdapter} for screen-scraping from Channel4's 4OD website
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class C4BrandAtoZAdapter extends BaseC4PlaylistClient implements SiteSpecificAdapter<Playlist> {

	private static final String baseUri = "http://www.channel4.com/programmes/atoz";

	public C4BrandAtoZAdapter() throws JAXBException {
		this(new C4BrandAtoZClient(), new C4AtomBackedBrandAdapter());
	}
	
	public C4BrandAtoZAdapter(RemoteSiteClient<BrandListingPage> brandListClient, SiteSpecificAdapter<Brand> brandClient) {
		super(brandListClient, brandClient);
	}

	public boolean canFetch(String uri) {
		return uri.startsWith(baseUri) && uri.length() > baseUri.length();
	}
}
