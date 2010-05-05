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

package org.uriplay.remotesite.bbc;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.DescriptionMode;
import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGeneratorFactory;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Playlist;
import org.uriplay.persistence.RemoteSiteRefresher;
import org.uriplay.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesVersion;
import org.uriplay.remotesite.synd.SyndicationSource;

import com.google.common.collect.Sets;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndLink;

/**
 * {@link BeanGraphExtractor} for BBC iPlayer content.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BbcIplayerGraphExtractor implements BeanGraphExtractor<SyndicationSource> {

	private static final Log log = LogFactory.getLog(RemoteSiteRefresher.class);
	
	private final RemoteSiteClient<SlashProgrammesRdf> episodeClient;
	private final RemoteSiteClient<SlashProgrammesVersionRdf> versionClient;
	
	private final BbcProgrammeGraphExtractor programmeGraphExtractor;

	public BbcIplayerGraphExtractor(IdGeneratorFactory idGeneratorFactory) throws JAXBException {
		this(new BbcSlashProgrammesEpisodeRdfClient(), new BbcSlashProgrammesVersionRdfClient(), idGeneratorFactory);
	}
	
	public BbcIplayerGraphExtractor(RemoteSiteClient<SlashProgrammesRdf> episodeClient, 
			                        RemoteSiteClient<SlashProgrammesVersionRdf> versionClient, 
			                        IdGeneratorFactory idGeneratorFactory) {
		this.episodeClient = episodeClient;
		this.versionClient = versionClient;
		this.programmeGraphExtractor = new BbcProgrammeGraphExtractor(idGeneratorFactory, new SeriesFetchingBbcSeriesNumberResolver());
	}

	@SuppressWarnings("unchecked")
	public Representation extractFrom(SyndicationSource source) {
		
		Representation representation = new Representation();
		
		Set<String> brandUris = Sets.newHashSet();
		Set<String> orphanEpisodeUris = Sets.newHashSet();
		
		for (SyndEntry entry : (List<SyndEntry>) source.getFeed().getEntries()) {
			
			// remove this clause to include radio content
			if (isRadioProgramme(entry)) {
				continue;
			}
			
			String episodeUri = episodeUriFrom(selfLink(entry));
			if (episodeUri == null || !BbcProgrammeAdapter.SLASH_PROGRAMMES_URL_PATTERN.matcher(episodeUri).matches()) {
				continue;
			}
			
			SlashProgrammesRdf slashProgrammesEpisode = readSlashProgrammesDataForEpisode(episodeUri);
			SlashProgrammesVersionRdf slashProgrammesVersion = readSlashProgrammesDataForVersion(slashProgrammesEpisode.episode().versions().get(0));
			
			Representation representationOfEpisode = programmeGraphExtractor.extractFrom(new BbcProgrammeSource(episodeUri, slashProgrammesUri(episodeUri).replace(".rdf", ""), slashProgrammesEpisode, slashProgrammesVersion));
			
			representation.mergeIn(representationOfEpisode);

			SyndLink relatedLink = relatedLinkFrom(entry);
			String brandUri = brandLinkFrom(relatedLink);
			if (brandUri == null || brandUri.equals(episodeUri)) {
				orphanEpisodeUris.add(episodeUri);
				continue; // no associated brand is specified as being related to this item.
			}
			
			if (representation.getType(brandUri) == null) {
				addBrand(representation, brandUri, relatedLink);
				brandUris.add(brandUri);
			}
			
			MutablePropertyValues values = representation.getValues(brandUri);
			PropertyValue propertyValue = values.getPropertyValue("items");
			Set<String> items = (Set<String>) propertyValue.getValue();
			items.add(episodeUri);
		}
		
		putBrandsInPlaylist(representation, source.getUri(), brandUris, orphanEpisodeUris);
		return representation;
	}
	
	private void putBrandsInPlaylist(Representation representation, String playlistUri, Set<String> brandUris, Set<String> orphanEpsiodeUris) {
		representation.addUri(playlistUri);
		representation.addType(playlistUri, Playlist.class);
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("playlists", brandUris);
		mpvs.addPropertyValue("items", orphanEpsiodeUris);
		mpvs.addPropertyValue("publisher", BbcProgrammeGraphExtractor.BBC_PUBLISHER);
		representation.addValues(playlistUri, mpvs);
	}

	@SuppressWarnings("unchecked")
	private boolean isRadioProgramme(SyndEntry entry) {
		List<SyndCategory> categories = entry.getCategories();
		for (SyndCategory category : categories) {
			if ("Radio".equalsIgnoreCase(category.getName())) {
				return true;
			}
		}
		return false;
	}

	private SlashProgrammesVersionRdf readSlashProgrammesDataForVersion(SlashProgrammesVersion slashProgrammesVersion) {
		try {
			return versionClient.get(slashProgrammesUri(slashProgrammesVersion));
		} catch (Exception e) {
			log.warn(e);
			return null;
		}
	}

	private SlashProgrammesRdf readSlashProgrammesDataForEpisode(String episodeUri) {
		try {
			return episodeClient.get(slashProgrammesUri(episodeUri));
		} catch (Exception e) {
			log.warn(e);
			return null;
		}
	}

	private String slashProgrammesUri(String episodeUri) {
		return episodeUri.replaceAll("/iplayer/episode", "/programmes") + ".rdf";
	}
	
	private String slashProgrammesUri(SlashProgrammesVersion slashProgrammesVersion) {
		return "http://www.bbc.co.uk" + slashProgrammesVersion.resourceUri().replace("#programme", "") + ".rdf";
	}
	
	private void addBrand(Representation representation, String brandUri, SyndLink relatedLink) {
		representation.addType(brandUri, Brand.class);
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("items", Sets.newHashSet());
		mpvs.addPropertyValue("title", brandTitleFrom(relatedLink));
		mpvs.addPropertyValue("curie", BbcUriCanonicaliser.curieFor(brandUri));
		mpvs.addPropertyValue("publisher", BbcProgrammeGraphExtractor.BBC_PUBLISHER);
		representation.addValues(brandUri, mpvs);
		representation.addUri(brandUri);
	}

	private String brandTitleFrom(SyndLink link) {
		return link.getTitle();
	}

	private String episodeUriFrom(String selfLink) {
		Pattern pidPattern = Pattern.compile("http://feeds\\.bbc\\.co\\.uk.*?/b00(.+)");
		Matcher matcher = pidPattern.matcher(selfLink);
		if (matcher.find()) {
			return "http://www.bbc.co.uk/programmes/b00" + matcher.group(1);
		}
		log.warn("Could not extract programme id from " + selfLink);
		return null;
	}

	private String brandLinkFrom(SyndLink link) {
		if (link == null) {
			return null;
		}
		return link.getHref().replace("/microsite", "");
	}

	@SuppressWarnings("unchecked")
	private SyndLink relatedLinkFrom(SyndEntry entry) {
		for (SyndLink link : (List<SyndLink>) entry.getLinks()) {
			if ("related".equals(link.getRel())) {
				return link;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private String selfLink(SyndEntry entry) {
		for (SyndLink link : (List<SyndLink>) entry.getLinks()) {
			if ("self".equals(link.getRel())) {
				return link.getHref();
			}
		}
		return null;
	}
}
