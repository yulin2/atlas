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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Description;
import org.uriplay.media.entity.Item;
import org.uriplay.persistence.system.RequestTimer;
import org.uriplay.remotesite.ContentExtractor;
import org.uriplay.remotesite.SiteSpecificAdapter;
import org.uriplay.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesContainerRef;
import org.uriplay.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesVersion;

public class BbcProgrammeAdapter implements SiteSpecificAdapter<Description> {

	static final Pattern SLASH_PROGRAMMES_URL_PATTERN = Pattern.compile("^http://www\\.bbc\\.co\\.uk/programmes/([^/\\.]+)$");
	
	private final BbcSlashProgrammesEpisodeRdfClient episodeClient;
	private final ContentExtractor<BbcProgrammeSource, Item> contentExtractor;

	private final BbcSlashProgrammesVersionRdfClient versionClient;

	private final Log log = LogFactory.getLog(getClass());
	
	public BbcProgrammeAdapter() {
		this(new BbcSlashProgrammesEpisodeRdfClient(), new BbcSlashProgrammesVersionRdfClient(), new BbcProgrammeGraphExtractor(new SeriesFetchingBbcSeriesNumberResolver()));
	}
	
	public BbcProgrammeAdapter(BbcSlashProgrammesEpisodeRdfClient episodeClient, BbcSlashProgrammesVersionRdfClient versionClient, ContentExtractor<BbcProgrammeSource, Item> propertyExtractor) {
		this.versionClient = versionClient;
		this.episodeClient = episodeClient;
		this.contentExtractor = propertyExtractor;
	}

	public boolean canFetch(String uri) {
		Matcher matcher = SLASH_PROGRAMMES_URL_PATTERN.matcher(uri);
		return matcher.matches();
	}

	public Description fetch(String uri, RequestTimer timer) {
		try {
			SlashProgrammesRdf content = readSlashProgrammesDataForEpisode(uri);
			if (content == null) {
				return null;
			}
			
			if (content.episode() != null) {
				SlashProgrammesVersionRdf version = readSlashProgrammesDataForVersion(content.episode().versions().get(0));
				BbcProgrammeSource source = new BbcProgrammeSource(uri, uri, content, version).forAnUnavailableProgramme();
				return contentExtractor.extract(source);
			}
			if (content.brand() != null) {
				/* If the brand wasn't already in the DB then it's not available -- create an empty brand with no items */
				return emptyBrand(content.brand());
			}
			return null;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static final Pattern IMAGE_STEM = Pattern.compile("^(.+)_[0-9]+_[0-9]+\\.[a-zA-Z]+$");
	private static final BbcProgrammesGenreMap genreMap = new BbcProgrammesGenreMap();
	
	private Brand emptyBrand(SlashProgrammesContainerRef brandRef) {
		String brandUri = brandRef.uri();
		Brand brand = new Brand(brandUri, BbcUriCanonicaliser.curieFor(brandUri));
		brand.setTitle(brandRef.title());
		if (brandRef.getDepiction() != null) {
			Matcher matcher = IMAGE_STEM.matcher(brandRef.getDepiction().resourceUri());
			if (matcher.matches()) {
				String base = matcher.group(1);
				brand.setImage(base + BbcProgrammeGraphExtractor.FULL_IMAGE_EXTENSION);
				brand.setThumbnail(base + BbcProgrammeGraphExtractor.THUMBNAIL_EXTENSION);
			}
		}
		brand.setGenres(genreMap.map(brandRef.genreUris()));
		brand.setDescription(brandRef.description());
		brand.setPublisher(BbcProgrammeGraphExtractor.BBC_PUBLISHER);
		return brand;
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
			return episodeClient.get(episodeUri + ".rdf");
		} catch (Exception e) {
			log.warn(e);
			return null;
		}
	}
	
	private String slashProgrammesUri(SlashProgrammesVersion slashProgrammesVersion) {
		return "http://www.bbc.co.uk" + slashProgrammesVersion.resourceUri().replace("#programme", "") + ".rdf";
	}
}
