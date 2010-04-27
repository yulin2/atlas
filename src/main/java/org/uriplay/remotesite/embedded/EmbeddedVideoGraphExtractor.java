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


package org.uriplay.remotesite.embedded;

import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.DescriptionMode;
import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGenerator;
import org.jherd.beans.id.IdGeneratorFactory;
import org.springframework.beans.MutablePropertyValues;
import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Episode;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Version;
import org.uriplay.remotesite.html.HtmlDescriptionOfItem;
import org.uriplay.remotesite.html.HtmlDescriptionSource;

import com.google.common.collect.Sets;

public class EmbeddedVideoGraphExtractor implements BeanGraphExtractor<HtmlDescriptionSource>  {

	private final IdGeneratorFactory idGeneratorFactory;

	public EmbeddedVideoGraphExtractor(IdGeneratorFactory idGen) {
		this.idGeneratorFactory = idGen;
	}
	
	public Representation extractFrom(HtmlDescriptionSource source) {
	
		IdGenerator idGenerator = idGeneratorFactory.create();
		
		Representation representation = new Representation();
		
		HtmlDescriptionOfItem item = source.getItem();
				
		String versionId = idGenerator.getNextId();
		String encodingId = idGenerator.getNextId();
		String locationId = idGenerator.getNextId();
		
		addEpisode(representation, source.getUri(), item, versionId);
		addVersion(representation, versionId, encodingId);
		addEncoding(representation, encodingId, locationId);
		addEmbedCodeLocationTo(representation, locationId, item.getEmbedObject());
		
		return representation;
	}
	
	private void addEpisode(Representation representation, String uri, HtmlDescriptionOfItem item, String versionId) {
		
		representation.addType(uri, Episode.class);
		representation.addUri(uri);

		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("title", item.getTitle());
		mpvs.addPropertyValue("versions", Sets.newHashSet(versionId));
		
		representation.addValues(uri, mpvs);
	}
	
	private void addEmbedCodeLocationTo(Representation representation, String locationId, String embedCode) {
		representation.addType(locationId, Location.class);
		representation.addAnonymous(locationId);
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("embedCode", embedCode);
		mpvs.addPropertyValue("transportType", TransportType.EMBEDOBJECT.toString().toLowerCase());
		mpvs.addPropertyValue("transportSubType", "html");
		representation.addValues(locationId, mpvs);
	}

	private void addEncoding(Representation representation, String encodingId, String locationId) {
		representation.addType(encodingId, Encoding.class);
		representation.addAnonymous(encodingId);
		
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("availableAt", Sets.newHashSet(locationId));
		representation.addValues(encodingId, mpvs);
	}


	private void addVersion(Representation representation, String versionId, String encodingId) {

		representation.addType(versionId, Version.class);
		representation.addAnonymous(versionId);
		
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("manifestedAs", Sets.newHashSet(encodingId));
		representation.addValues(versionId, mpvs);
	}

	public Representation extractFrom(HtmlDescriptionSource src, DescriptionMode mode) {
		return null;
	}

}
