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

import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGeneratorFactory;
import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.SiteSpecificRepresentationAdapter;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.jherd.remotesite.timing.RequestTimer;

/**
 * {@link SiteSpecificRepresentationAdapter} for ITV Brands.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class ItvBrandAdapter implements SiteSpecificRepresentationAdapter {

	private final RemoteSiteClient<List<ItvProgramme>> client;
	private final BeanGraphExtractor<ItvBrandSource> propertyExtractor;

	private static final String ITV_URI = "http://www.itv.com/_data/xml/CatchUpData/CatchUp360/CatchUpMenu.xml";

	public ItvBrandAdapter(IdGeneratorFactory idGeneratorFactory) throws JAXBException {
		this(new ItvCatchupClient(), new ItvGraphExtractor(idGeneratorFactory));
	}
	
	public ItvBrandAdapter(RemoteSiteClient<List<ItvProgramme>> client, BeanGraphExtractor<ItvBrandSource> propertyExtractor) {
		this.client = client;
		this.propertyExtractor = propertyExtractor;
	}

	public Representation fetch(String uri, RequestTimer timer) {
		try {
			List<ItvProgramme> itvBrands = client.get(uri);
			return propertyExtractor.extractFrom(new ItvBrandSource(itvBrands, uri));
		} catch (Exception e) {
			throw new FetchException("Problem processing data from ITV", e);
		}
	}

	public boolean canFetch(String uri) {
		return uri.equals(ITV_URI);
	}
}
