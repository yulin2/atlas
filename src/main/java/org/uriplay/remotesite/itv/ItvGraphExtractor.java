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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.DescriptionMode;
import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGenerator;
import org.jherd.beans.id.IdGeneratorFactory;
import org.jherd.remotesite.SiteSpecificRepresentationAdapter;
import org.springframework.beans.MutablePropertyValues;
import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Episode;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Version;

import com.google.common.collect.Sets;

/**
 * {@link SiteSpecificRepresentationAdapter} for content from itv.com
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class ItvGraphExtractor implements BeanGraphExtractor<ItvBrandSource> {

	private static final String ITV_PUBLISHER = "itv.com";
	
	private final IdGeneratorFactory idGeneratorFactory;

	public ItvGraphExtractor(IdGeneratorFactory idGeneratorFactory) {
		this.idGeneratorFactory = idGeneratorFactory;
	}

	
	private void addBrand(Representation representation, ItvProgramme brand, Set<String> episodeUris) {
	
		representation.addType(brand.url(), Brand.class);
		representation.addUri(brand.url());

		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("items", episodeUris);
		mpvs.addPropertyValue("title", brand.title());
		mpvs.addPropertyValue("curie", curieFrom(brand.url()));
		mpvs.addPropertyValue("publisher", ITV_PUBLISHER);
		representation.addValues(brand.url(), mpvs);
	}
	
	private void addEpisode(Representation representation, ItvEpisode episode, ItvProgramme brand, String versionId) {
	
		representation.addType(episode.url(), Episode.class);
		representation.addUri(episode.url());

		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("title", episode.date());
		mpvs.addPropertyValue("description", episode.description());
		mpvs.addPropertyValue("publisher", ITV_PUBLISHER);
		mpvs.addPropertyValue("containedIn", Sets.newHashSet(brand.url()));
		mpvs.addPropertyValue("thumbnail", brand.thumbnail());
		mpvs.addPropertyValue("versions", Sets.newHashSet(versionId));
		mpvs.addPropertyValue("curie", curieFrom(episode.url()));
		mpvs.addPropertyValue("isLongForm", true);
		
		representation.addValues(episode.url(), mpvs);
	}
	
	private static Pattern curiePattern = Pattern.compile(".*ViewType=(\\d+).*&Filter=(\\d+).*");
	
	private String curieFrom(String url) {
		Matcher matcher = curiePattern.matcher(url);
		if (matcher.find()) {
			return "itv:" + matcher.group(1) + "-" + matcher.group(2);
		}
		return null;
	}


	public Representation extractFrom(ItvBrandSource source) {
	
		IdGenerator idGenerator = idGeneratorFactory.create();
		
		Representation representation = new Representation();
		
		List<ItvProgramme> brands = source.brands();
		for (ItvProgramme brand : brands) {
			
			Set<String> episodeUris = Sets.newHashSet();
			
			for (ItvEpisode episode : brand.episodes()) {
				
				String versionId = idGenerator.getNextId();
				String encodingId = idGenerator.getNextId();
				String locationId = idGenerator.getNextId();
				
				addEpisode(representation, episode, brand, versionId);
				addVersion(representation, versionId, encodingId);
				addEncoding(representation, encodingId, locationId);
				addLocation(representation, locationId, episode);
				episodeUris.add(episode.url());
			}
			
			addBrand(representation, brand, episodeUris);
		}
		
		return representation;
	}

	private void addLocation(Representation representation, String locationId, ItvEpisode episode) {
		representation.addType(locationId, Location.class);
		representation.addAnonymous(locationId);
		
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("uri", episode.url());
		mpvs.addPropertyValue("transportType", TransportType.HTMLEMBED.toString().toLowerCase());
		mpvs.addPropertyValue("available", true);
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


	public Representation extractFrom(ItvBrandSource source, DescriptionMode mode) {
		// TODO Auto-generated method stub
		return null;
	}

}
