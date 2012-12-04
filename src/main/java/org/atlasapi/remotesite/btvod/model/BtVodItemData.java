package org.atlasapi.remotesite.btvod.model;

import java.util.List;
import java.util.Set;


import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.common.collect.Lists;

public class BtVodItemData {
    private final String uri;
    private final String title;
    private final String description;
    private final int year;
    private final String language;
    private final String certificate;
    private final Set<String> genres;
    private final List<BtVodLocationData> locations;
    private final String selfLink;
    private final String externalId;
    private final Optional<Integer> seriesNumber;
    private final Optional<Integer> episodeNumber;
    private final Optional<String> container;
    private final Optional<String> containerTitle;
    private final Optional<String> containerSelfLink;
    private final Optional<String> containerExternalId;
    
    public static BtVodItemDataBuilder builder() {
        return new BtVodItemDataBuilder();
    }
    
    private BtVodItemData(String uri, String title, String description, int year, String language, 
            String certificate, Set<String> genres, List<BtVodLocationData> location, String selfLink, 
            String externalId, Optional<Integer> seriesNumber, Optional<Integer> episodeNumber, 
            Optional<String> container, Optional<String> containerTitle, Optional<String> containerSelfLink, 
            Optional<String> containerExternalId) {
        this.uri = uri;
        this.title = title;
        this.description = description;
        this.year = year;
        this.language = language;
        this.certificate = certificate;
        this.genres = genres;
        this.locations = location;
        this.selfLink = selfLink;
        this.externalId = externalId;
        this.seriesNumber = seriesNumber;
        this.episodeNumber = episodeNumber;
        this.container = container;
        this.containerTitle = containerTitle;
        this.containerSelfLink = containerSelfLink;
        this.containerExternalId = containerExternalId;
    }
    
    public String getUri() {
        return uri;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getYear() {
        return year;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public String getCertificate() {
        return certificate;
    }
    
    public Set<String> getGenres() {
        return genres;
    }
    
    public List<BtVodLocationData> getLocations() {
        return locations;
    }
    
    public String getSelfLink() {
        return selfLink;
    }
    
    public String getExternalId() {
        return externalId;
    }
    
    public Optional<Integer> getSeriesNumber() {
        return seriesNumber;
    }
    
    public Optional<Integer> getEpisodeNumber() {
        return episodeNumber;
    }
    
    public Optional<String> getContainer() {
        return container;
    }
    
    public Optional<String> getContainerTitle() {
        return containerTitle;
    }

    public Optional<String> getContainerSelfLink() {
        return containerSelfLink;
    }

    public Optional<String> getContainerExternalId() {
        return containerExternalId;
    }

    public static class BtVodItemDataBuilder {
        private static final String URI_PREFIX = "http://bt.com/titles/";
        private static final String GENRE_PREFIX = "http://bt.com/genres/";
        private static final String CONTAINER_PREFIX = "http://bt.com/title_groups/";
        
        private String uri;
        private String title;
        private String description;
        private int year;
        private String language;
        private String certificate;
        private Set<String> genres;
        private List<BtVodLocationData> locations;
        private String selfLink;
        private String externalId;
        private Optional<Integer> seriesNumber;
        private Optional<Integer> episodeNumber;
        private Optional<String> container;
        private Optional<String> containerTitle;
        private Optional<String> containerSelfLink;
        private Optional<String> containerExternalId;
        
        public BtVodItemData build() {
            return new BtVodItemData(uri, title, description, year, language, certificate, genres, 
                    locations, selfLink, externalId, seriesNumber, episodeNumber, container, 
                    containerTitle, containerSelfLink, containerExternalId);
        }
        
        private BtVodItemDataBuilder() {
            genres = Sets.newHashSet();
            locations = Lists.newArrayList();
            seriesNumber = Optional.absent();
            episodeNumber = Optional.absent();
            container = Optional.absent();
            containerTitle = Optional.absent();
            containerSelfLink = Optional.absent();
            containerExternalId = Optional.absent();
        }

        public void setUri(String id) {
            this.uri = URI_PREFIX + id;
        }

        public BtVodItemDataBuilder withUri(String id) {
            setUri(id);
            return this;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public BtVodItemDataBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BtVodItemDataBuilder withDescription(String description) {
            this.description = description;
            return this;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public BtVodItemDataBuilder withYear(int year) {
            this.year = year;
            return this;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public BtVodItemDataBuilder withLanguage(String language) {
            this.language = language;
            return this;
        }

        public void setCertificate(String certificate) {
            this.certificate = certificate;
        }

        public BtVodItemDataBuilder withCertificate(String certificate) {
            this.certificate = certificate;
            return this;
        }

        public void addGenre(String genreKey) {
            this.genres.add(GENRE_PREFIX + genreKey);
        }
        
        public BtVodItemDataBuilder withGenre(String genreKey) {
            addGenre(genreKey);
            return this;
        }

        public void setGenres(Set<String> genreKeys) {
            genres.clear();
            for(String genreKey : genreKeys) {
                addGenre(genreKey);
            }
        }

        public BtVodItemDataBuilder withGenres(Set<String> genreKeys) {
            setGenres(genreKeys);
            return this;
        }

        public void addLocation(BtVodLocationData location) {
            this.locations.add(location);
        }

        public BtVodItemDataBuilder withLocation(BtVodLocationData location) {
            this.locations.add(location);
            return this;
        }

        public void setLocations(List<BtVodLocationData> locations) {
            this.locations = locations;
        }

        public BtVodItemDataBuilder withLocations(List<BtVodLocationData> locations) {
             setLocations(locations);
             return this;
        }

        public void setSelfLink(String selfLink) {
            this.selfLink = selfLink;
        }

        public BtVodItemDataBuilder withSelfLink(String selfLink) {
            this.selfLink = selfLink;
            return this;
        }

        public void setExternalId(String externalId) {
            this.externalId = externalId;
        }

        public BtVodItemDataBuilder withExternalId(String externalId) {
            this.externalId = externalId;
            return this;
        }

        public void setSeriesNumber(int seriesNumber) {
            this.seriesNumber = Optional.fromNullable(seriesNumber);
        }

        public BtVodItemDataBuilder withSeriesNumber(int seriesNumber) {
            this.seriesNumber = Optional.fromNullable(seriesNumber);
            return this;
        }

        public void setEpisodeNumber(int episodeNumber) {
            this.episodeNumber = Optional.fromNullable(episodeNumber);
        }

        public BtVodItemDataBuilder withEpisodeNumber(int episodeNumber) {
            this.episodeNumber = Optional.fromNullable(episodeNumber);
            return this;
        }

        public void setContainer(String containerId) {
            this.container = Optional.of(CONTAINER_PREFIX + containerId);
        }

        public BtVodItemDataBuilder withContainer(String containerId) {
            setContainer(containerId);
            return this;
        }

        public void setContainerTitle(String containerTitle) {
            this.containerTitle = Optional.fromNullable(containerTitle);
        }

        public BtVodItemDataBuilder withContainerTitle(String containerTitle) {
            this.containerTitle = Optional.fromNullable(containerTitle);
            return this;
        }

        public void setContainerSelfLink(String containerSelfLink) {
            this.containerSelfLink = Optional.fromNullable(containerSelfLink);
        }

        public BtVodItemDataBuilder withContainerSelfLink(String containerSelfLink) {
            this.containerSelfLink = Optional.fromNullable(containerSelfLink);
            return this;
        }

        public void setContainerExternalId(String containerExternalId) {
            this.containerExternalId = Optional.fromNullable(containerExternalId);
        }

        public BtVodItemDataBuilder withContainerExternalId(String containerExternalId) {
            this.containerExternalId = Optional.fromNullable(containerExternalId);
            return this;
        }
        
    }
}