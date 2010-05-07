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

package org.uriplay.remotesite.itv;

import java.util.List;

import javax.xml.bind.JAXBException;

import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.SiteSpecificAdapter;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.jherd.remotesite.timing.RequestTimer;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Playlist;
import org.uriplay.remotesite.ContentExtractor;

/**
 * {@link SiteSpecificAdapter} for ITV Brands.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class ItvBrandAdapter implements SiteSpecificAdapter<Playlist> {

	private final RemoteSiteClient<List<ItvProgramme>> client;
	private final ContentExtractor<ItvBrandSource, List<Brand>> propertyExtractor;

	private static final String ITV_URI = "http://www.itv.com/_data/xml/CatchUpData/CatchUp360/CatchUpMenu.xml";

	public ItvBrandAdapter() throws JAXBException {
		this(new ItvCatchupClient(), new ItvGraphExtractor());
	}
	
	public ItvBrandAdapter(RemoteSiteClient<List<ItvProgramme>> client,  ContentExtractor<ItvBrandSource, List<Brand>> itvGraphExtractor) {
		this.client = client;
		this.propertyExtractor = itvGraphExtractor;
	}

	public Playlist fetch(String uri, RequestTimer timer) {
		try {
			List<ItvProgramme> itvBrands = client.get(uri);
			Playlist playlist = new Playlist();
			playlist.setCanonicalUri(ITV_URI);
			playlist.setTitle("ITV CatchUp Menu");
			playlist.setPlaylists((List) propertyExtractor.extract(new ItvBrandSource(itvBrands, uri)));
			return playlist;
		} catch (Exception e) {
			throw new FetchException("Problem processing data from ITV", e);
		}
	}

	public boolean canFetch(String uri) {
		return uri.equals(ITV_URI);
	}
}
