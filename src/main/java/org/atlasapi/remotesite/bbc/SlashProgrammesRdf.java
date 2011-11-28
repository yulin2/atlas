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
import org.atlasapi.media.vocabulary.OWL;
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
@XmlRootElement(name = "RDF", namespace = RDF.NS)
class SlashProgrammesRdf {

    @XmlElement(namespace = PO.NS, name = "Episode")
    private SlashProgrammesEpisode episode;

    @XmlElement(namespace = PO.NS, name = "Clip")
    private SlashProgrammesEpisode clip;

    @XmlElement(namespace = PO.NS, name = "Brand")
    private SlashProgrammesContainerRef brand;

    @XmlElement(namespace = PO.NS, name = "Series")
    private SlashProgrammesSeriesContainer series;

    @XmlElement(namespace = RDF.NS, name = "Description")
    private SlashProgrammesDescription description;

    static class SlashProgrammesDescription {

        @XmlElement(namespace = OWL.NS, name = "sameAs")
        private Set<SlashProgrammesSameAs> sameAs;

        @XmlElement(namespace = RDF.NS, name = "type")
        private Set<SlashProgrammesType> type;

        public Set<SlashProgrammesSameAs> getSameAs() {
            return sameAs;
        }

        public Set<SlashProgrammesType> getType() {
            return type;
        }

        public SlashProgrammesDescription withSameAs(Set<SlashProgrammesSameAs> sameAs) {
            this.sameAs = sameAs;
            return this;
        }

        public SlashProgrammesDescription withTypes(Set<SlashProgrammesType> types) {
            this.type = types;
            return this;
        }

    }

    static class SlashProgrammesType {

        @XmlAttribute(name = "resource", namespace = RDF.NS)
        private String resourceUri;

        public String resourceUri() {
            return resourceUri;
        }

        public SlashProgrammesType withResourceUri(String uri) {
            resourceUri = uri;
            return this;
        }

    }

    static class SlashProgrammesSameAs {

        @XmlAttribute(name = "resource", namespace = RDF.NS)
        private String resourceUri;

        public String resourceUri() {
            return resourceUri;
        }

        public SlashProgrammesSameAs withResourceUri(String uri) {
            resourceUri = uri;
            return this;
        }

    }

    static class SlashProgrammesEpisode extends SlashProgrammesBase {

        @XmlElement(namespace = PO.NS, name = "position")
        private Integer position;

        @XmlElement(namespace = PO.NS, name = "version")
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
            if (this.genres == null) {
                this.genres = Sets.newHashSet();
            }
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

        @XmlAttribute(name = "about", namespace = RDF.NS)
        protected String uri;

        @XmlElement(namespace = DC.NS, name = "title")
        protected String title;

        @XmlElement(namespace = FOAF.NS, name = "depiction")
        protected FoafDepiction depiction;

        @XmlElement(namespace = PO.NS, name = "short_synopsis")
        protected String shortSynopsis;

        @XmlElement(namespace = PO.NS, name = "medium_synopsis")
        protected String mediumSynopsis;

        @XmlElement(namespace = PO.NS, name = "long_synopsis")
        protected String longSynopsis;

        @XmlElement(namespace = PO.NS, name = "masterbrand")
        private SlashProgrammesMasterbrand masterbrand;

        @XmlElement(namespace = PO.NS, name = "genre")
        protected Set<SlashProgrammesGenre> genres;

        @XmlElement(namespace = PO.NS, name = "subject")
        protected Set<SlashProgrammesTopic> subjects;

        @XmlElement(namespace = PO.NS, name = "place")
        protected Set<SlashProgrammesTopic> places;

        @XmlElement(namespace = PO.NS, name = "person")
        protected Set<SlashProgrammesTopic> people;

        @XmlElement(namespace = PO.NS, name = "format")
        protected SlashProgrammesFormat format;

        @XmlElement(namespace = PO.NS, name = "clip")
        protected Set<SlashProgrammesClip> clips;

        public String description() {
            if (!StringUtils.isEmpty(longSynopsis)) {
                return longSynopsis;
            }
            if (!StringUtils.isEmpty(mediumSynopsis)) {
                return mediumSynopsis;
            }
            if (!StringUtils.isEmpty(shortSynopsis)) {
                return shortSynopsis;
            }
            return null;
        }

        public String title() {
            return title;
        }

        public Set<SlashProgrammesGenre> genres() {
            return genres;
        }

        public SlashProgrammesFormat format() {
            return format;
        }

        public boolean isFilmFormat() {
            return format != null && "/programmes/formats/films#format".equals(format.getResourceUri());
        }

        public Set<String> genreUris() {
            if (genres == null || genres.isEmpty()) {
                return Collections.emptySet();
            }
            Set<String> uris = Sets.newHashSet();
            for (SlashProgrammesGenre genre : genres) {
                uris.add("http://www.bbc.co.uk" + genre.resourceUri().replace("#genre", ""));
            }
            return uris;
        }

        public Set<SlashProgrammesClip> clips() {
            return clips;
        }

        public FoafDepiction getDepiction() {
            return depiction;
        }

        public String uri() {
            return BASE_URI + uri.replace("#programme", "");
        }

        public SlashProgrammesMasterbrand getMasterbrand() {
            return masterbrand;
        }

        public Set<SlashProgrammesTopic> subjects() {
            return this.subjects;
        }

        public Set<SlashProgrammesTopic> people() {
            return this.people;
        }

        public Set<SlashProgrammesTopic> places() {
            return this.places;
        }
    }

    static class SlashProgrammesSeriesContainer extends SlashProgrammesBase {

        @XmlElement(namespace = PO.NS, name = "episode")
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

        @XmlElement(name = "Episode", namespace = PO.NS)
        private SlashProgrammesContainerRef episode;

    }

    static class SlashProgrammesContainerRef extends SlashProgrammesBase {

        @XmlElement(namespace = PO.NS, name = "episode")
        protected List<SlashProgrammesEpisodeRef> episodes;

        @XmlElement(namespace = PO.NS, name = "series")
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

    static class SlashProgrammesClip {

        @XmlAttribute(name = "resource", namespace = RDF.NS)
        private String resourceUri;

        public String resourceUri() {
            return resourceUri;
        }

        public SlashProgrammesClip withResourceUri(String uri) {
            resourceUri = uri;
            return this;
        }

    }

    static class SlashProgrammesVersion {

        @XmlAttribute(name = "resource", namespace = RDF.NS)
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

        @XmlAttribute(name = "resource", namespace = RDF.NS)
        private String resourceUri;

        public String resourceUri() {
            return resourceUri;
        }

        public SlashProgrammesGenre withResourceUri(String uri) {
            resourceUri = uri;
            return this;
        }
    }

    static class SlashProgrammesTopic {

        @XmlAttribute(name = "resource", namespace = RDF.NS)
        private String resourceUri;

        public String resourceUri() {
            return resourceUri;
        }

        public SlashProgrammesTopic withResourceUri(String uri) {
            resourceUri = uri;
            return this;
        }
    }

    static class SlashProgrammesMasterbrand {

        @XmlAttribute(name = "resource", namespace = RDF.NS)
        private String resourceUri;

        public SlashProgrammesMasterbrand withResourceUri(String uri) {
            this.resourceUri = uri;
            return this;
        }

        public String getResourceUri() {
            return resourceUri;
        }

    }

    static class SlashProgrammesFormat {

        @XmlAttribute(name = "resource", namespace = RDF.NS)
        private String resourceUri;

        public SlashProgrammesFormat withResourceUri(String uri) {
            this.resourceUri = uri;
            return this;
        }

        public String getResourceUri() {
            return resourceUri;
        }

    }

    static class SlashProgrammesEpisodeRef {

        @XmlAttribute(name = "resource", namespace = RDF.NS)
        private String resourceUri;

        public String resourceUri() {
            return resourceUri;
        }
    }

    static class SlashProgrammesSeriesRef {

        @XmlAttribute(name = "resource", namespace = RDF.NS)
        private String resourceUri;

        public String resourceUri() {
            return resourceUri;
        }
    }

    static String BASE_URI = "http://www.bbc.co.uk";

    static class FoafDepiction {

        @XmlAttribute(name = "resource", namespace = RDF.NS)
        private String resourceUri;

        public FoafDepiction(String uri) {
            resourceUri = uri;
        }

        public FoafDepiction() { /* for jaxb */
        }

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

    public SlashProgrammesEpisode clip() {
        return clip;
    }

    public SlashProgrammesRdf withClip(SlashProgrammesEpisode clip) {
        this.clip = clip;
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

    public SlashProgrammesRdf withDescription(SlashProgrammesDescription desc) {
        this.description = desc;
        return this;
    }

    public SlashProgrammesDescription description() {
        return this.description;
    }
}
