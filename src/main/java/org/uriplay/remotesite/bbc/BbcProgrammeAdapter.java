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

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGeneratorFactory;
import org.jherd.remotesite.SiteSpecificRepresentationAdapter;
import org.jherd.remotesite.timing.RequestTimer;
import org.uriplay.remotesite.bbc.SlashProgrammesEpisodeRdf.SlashProgrammesVersion;

public class BbcProgrammeAdapter implements SiteSpecificRepresentationAdapter {

	static final Pattern SLASH_PROGRAMMES_URL_PATTERN = Pattern.compile("^http://www\\.bbc\\.co\\.uk/programmes/([^/\\.]+)$");
	
	private final BbcSlashProgrammesEpisodeRdfClient episodeClient;
	private final BeanGraphExtractor<BbcProgrammeSource> propertyExtractor;

	private final BbcSlashProgrammesVersionRdfClient versionClient;

	private final Log log;
	
	public BbcProgrammeAdapter(IdGeneratorFactory idGeneratorFactory) throws JAXBException {
		this(new BbcSlashProgrammesEpisodeRdfClient(), new BbcSlashProgrammesVersionRdfClient(), new BbcProgrammeGraphExtractor(idGeneratorFactory, new SeriesFetchingBbcSeriesNumberResolver()));
	}
	
	public BbcProgrammeAdapter(BbcSlashProgrammesEpisodeRdfClient episodeClient, BbcSlashProgrammesVersionRdfClient versionClient, BeanGraphExtractor<BbcProgrammeSource> propertyExtractor) {
		this.versionClient = versionClient;
		this.episodeClient = episodeClient;
		this.propertyExtractor = propertyExtractor;
		this.log = LogFactory.getLog(getClass());
	}

	public boolean canFetch(String uri) {
		Matcher matcher = SLASH_PROGRAMMES_URL_PATTERN.matcher(uri);
		return matcher.matches();
	}

	public Representation fetch(String uri, RequestTimer timer) {
		try {
			SlashProgrammesEpisodeRdf episode = readSlashProgrammesDataForEpisode(uri);
			if (episode == null) {
				return new Representation();
			}
			SlashProgrammesVersionRdf version = readSlashProgrammesDataForVersion(episode.episode().versions().get(0));
			BbcProgrammeSource source = new BbcProgrammeSource(uri, uri, episode, version).forAnUnavailableProgramme();
			return propertyExtractor.extractFrom(source);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private SlashProgrammesVersionRdf readSlashProgrammesDataForVersion(SlashProgrammesVersion slashProgrammesVersion) {
		try {
			return versionClient.get(slashProgrammesUri(slashProgrammesVersion));
		} catch (Exception e) {
			log.warn(e);
			return null;
		}
	}

	private SlashProgrammesEpisodeRdf readSlashProgrammesDataForEpisode(String episodeUri) {
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
