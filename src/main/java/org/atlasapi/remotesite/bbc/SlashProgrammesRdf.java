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

package org.atlasapi.remotesite.bbc;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;
import org.atlasapi.media.vocabulary.PO;
import org.atlasapi.media.vocabulary.RDF;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
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
	private SlashProgrammesSeriesContainer series;
	
	static class SlashProgrammesEpisode extends SlashProgrammesBase {
		
		@XmlElement(namespace=PO.NS, name="position")
		private Integer position;

		@XmlElement(namespace=PO.NS, name="version")
		private List<SlashProgrammesVersion> versions;
		
		public Integer episodeNumber() {
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
		
		@XmlAttribute(name="about", namespace=RDF.NS)
		protected String uri;
		
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
		
		public FoafDepiction getDepiction() {
			return depiction;
		}
		
		public String uri() {
			return BASE_URI + uri.replace("#programme", "");
		}
	}
	
	static class SlashProgrammesSeriesContainer extends SlashProgrammesBase {

		@XmlElement(namespace=PO.NS, name="episode")
		private List<SlashProgrammesEpisodeWrapper> wrappedEpisodes;
		
		public List<SlashProgrammesContainerRef> episodes() {
			if (wrappedEpisodes == null) {
				return ImmutableList.of();
			}
			return Lists.transform(wrappedEpisodes, SlashProgrammesEpisodeWrapper.UNWRAP);
		}
		
		public List<String> episodeResourceUris() {
			if (wrappedEpisodes == null) {
				return ImmutableList.of();
			}
			return Lists.transform(wrappedEpisodes, new Function<SlashProgrammesEpisodeWrapper, String>() {
				@Override
				public String apply(SlashProgrammesEpisodeWrapper from) {
					return from.episode.uri;
				}
			});
		}
	}
	
	static class SlashProgrammesEpisodeWrapper {

		static Function<SlashProgrammesEpisodeWrapper, SlashProgrammesContainerRef> UNWRAP = new Function<SlashProgrammesEpisodeWrapper, SlashProgrammesContainerRef>() {
			@Override
			public SlashProgrammesContainerRef apply(SlashProgrammesEpisodeWrapper from) {
				return from.episode;
			}
		};
		
		@XmlElement(name="Episode", namespace=PO.NS)
		private SlashProgrammesContainerRef episode;
		
	}
	
	
	static class SlashProgrammesContainerRef extends SlashProgrammesBase {

		@XmlElement(namespace=PO.NS, name="episode")
		protected List<SlashProgrammesEpisodeRef> episodes;
		
		@XmlElement(namespace=PO.NS, name="series")
		protected List<SlashProgrammesSeriesRef> series;
		
		public SlashProgrammesContainerRef withUri(String uri) {
			this.uri = uri;
			return this;
		}

		public List<String> episodeResourceUris() {
			return Lists.transform(episodes, new Function<SlashProgrammesEpisodeRef, String>() {
				@Override
				public String apply(SlashProgrammesEpisodeRef from) {
					return from.resourceUri;
				}
			});
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
	
	static class SlashProgrammesEpisodeRef {

		@XmlAttribute(name="resource", namespace=RDF.NS)
		private String resourceUri;

		public String resourceUri() {
			return resourceUri;
		}
	}
	
	static class SlashProgrammesSeriesRef {

		@XmlAttribute(name="resource", namespace=RDF.NS)
		private String resourceUri;

		public String resourceUri() {
			return resourceUri;
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

	public SlashProgrammesSeriesContainer series() {
		return series;
	}
	
	public SlashProgrammesRdf withBrand(SlashProgrammesContainerRef brand) {
		this.brand = brand;
		return this;
	}

}
