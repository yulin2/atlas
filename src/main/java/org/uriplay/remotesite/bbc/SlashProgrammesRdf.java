/* Copyright 2009 British Broadcasting Corporation
   Copyright 2009 Meta Broadcast Ltd

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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;
import org.jherd.rdf.vocabulary.RDF;
import org.uriplay.media.vocabulary.PO;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DC;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@XmlRootElement(name="RDF", namespace=RDF.NS)
class SlashProgrammesRdf {

	@XmlElement(namespace=PO.NS, name="Episode")
	private SlashProgrammesEpisode episode;
	
	@XmlElement(namespace=PO.NS, name="Brand")
	private SlashProgrammesContainerRef brand;
	
	@XmlElement(namespace=PO.NS, name="Series")
	private SlashProgrammesContainerRef series;
	
	static class SlashProgrammesEpisode extends SlashProgrammesBase {
		
		@XmlElement(namespace=PO.NS, name="position")
		private int position;

		@XmlElement(namespace=PO.NS, name="version")
		private List<SlashProgrammesVersion> versions;
		
		public int episodeNumber() {
			return position;
		}
		
		public List<SlashProgrammesVersion> versions() {
			return versions;
		}
		
		public SlashProgrammesEpisode withVersion(SlashProgrammesVersion version) {
			this.versions = Lists.newArrayList(version);
			return this;
		}
		
		public SlashProgrammesEpisode inPosition(int pos) {
			position = pos;
			return this;
		}
		
		public SlashProgrammesEpisode withGenres(String... genres) {
			if (this.genres == null) { this.genres = Sets.newHashSet(); }
			for (String genre : genres) {
				this.genres.add(new SlashProgrammesGenre().withResourceUri(genre));
			}
			return this;
		}

		public SlashProgrammesEpisode withTitle(String title) {
			this.title = title;
			return this;
		}
		
		public SlashProgrammesEpisode withDescription(String description) {
			longSynopsis = description;
			return this;
		}
	}
	
	static class SlashProgrammesBase {
		
		@XmlElement(namespace=DC.NS, name="title")
		protected String title;
		
		@XmlElement(namespace=FOAF.NS, name="depiction")
		protected FoafDepiction depiction;
		
		@XmlElement(namespace=PO.NS, name="short_synopsis")
		protected String shortSynopsis;
		
		@XmlElement(namespace=PO.NS, name="medium_synopsis")
		protected String mediumSynopsis;
		
		@XmlElement(namespace=PO.NS, name="long_synopsis")
		protected String longSynopsis;
		
		@XmlElement(namespace=PO.NS, name="genre")
		protected Set<SlashProgrammesGenre> genres;
		
		public String description() {
			if (!StringUtils.isEmpty(longSynopsis))   { return longSynopsis; }
			if (!StringUtils.isEmpty(mediumSynopsis)) { return mediumSynopsis; }
			if (!StringUtils.isEmpty(shortSynopsis))  { return shortSynopsis; }
			return null;
		}

		public String title() {
			return title;
		}

		public Set<SlashProgrammesGenre> genres() {
			return genres;
		}

		public Set<String> genreUris() {
			if (genres == null || genres.isEmpty()) { return Collections.emptySet(); }
			Set<String> uris = Sets.newHashSet();
			for (SlashProgrammesGenre genre : genres) {
				uris.add("http://www.bbc.co.uk" + genre.resourceUri().replace("#genre", ""));
			}
			return uris;
		}
		
	}
	
	static class SlashProgrammesContainerRef extends SlashProgrammesBase {

		@XmlAttribute(name="about", namespace=RDF.NS)
		private String uri;
		
		public String uri() {
			return BASE_URI + uri.replace("#programme", "");
		}

		public SlashProgrammesContainerRef withUri(String uri) {
			this.uri = uri;
			return this;
		}
		
	}
	
	static class SlashProgrammesVersion {

		@XmlAttribute(name="resource", namespace=RDF.NS)
		private String resourceUri;

		public String resourceUri() {
			return resourceUri;
		}

		public SlashProgrammesVersion withResourceUri(String uri) {
			resourceUri = uri;
			return this;
		}

	}
	
	static class SlashProgrammesGenre {

		@XmlAttribute(name="resource", namespace=RDF.NS)
		private String resourceUri;

		public String resourceUri() {
			return resourceUri;
		}

		public SlashProgrammesGenre withResourceUri(String uri) {
			resourceUri = uri;
			return this;
		}

	}

	static String BASE_URI = "http://www.bbc.co.uk";
	
	static class FoafDepiction {
		
		@XmlAttribute(name="resource", namespace=RDF.NS)
		private String resourceUri;

		public FoafDepiction(String uri) {
			resourceUri = uri;
		}
		
		public FoafDepiction() { /* for jaxb */ }

		public String resourceUri() {
			return BASE_URI + resourceUri;
		}
		
	}

	public SlashProgrammesEpisode episode() {
		return episode;
	}
	
	public SlashProgrammesRdf withEpisode(SlashProgrammesEpisode episode) {
		this.episode = episode;
		return this;
	}

	public SlashProgrammesContainerRef brand() {
		return brand;
	}

	public SlashProgrammesContainerRef series() {
		return series;
	}
	
	public SlashProgrammesRdf withBrand(SlashProgrammesContainerRef brand) {
		this.brand = brand;
		return this;
	}

}
