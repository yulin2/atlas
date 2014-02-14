package org.atlasapi.remotesite.five;

import static org.atlasapi.media.entity.Specialization.FILM;

import java.io.StringReader;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.NodeFactory;
import nu.xom.Nodes;

import org.atlasapi.genres.GenreMap;
import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.http.HttpResponse;

public class FiveBrandProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(FiveBrandProcessor.class);
    
    private final static String WATCHABLES_URL_SUFFIX = "/watchables?expand=season%7Ctransmissions";
    private final ContentWriter writer;
    private final GenreMap genreMap = new FiveGenreMap();
    private final FiveEpisodeProcessor episodeProcessor;
    private final String baseApiUrl;
    private final RemoteSiteClient<HttpResponse> httpClient;

    public FiveBrandProcessor(ContentWriter writer, String baseApiUrl, RemoteSiteClient<HttpResponse> httpClient, Multimap<String, Channel> channelMap) {
        this.writer = writer;
        this.baseApiUrl = baseApiUrl;
        this.httpClient = httpClient;
        this.episodeProcessor = new FiveEpisodeProcessor(baseApiUrl, httpClient, channelMap);
    }
    
    public void processShow(Element element) {
        
        String id = childValue(element, "id");
        Brand brand = new Brand(getShowUri(id), getBrandCurie(id), Publisher.FIVE);
        
        brand.setTitle(childValue(element, "title"));
        
        Maybe<String> description = getDescription(element);
        if (description.hasValue()) {
            brand.setDescription(description.requireValue());
        }
        
        brand.setGenres(getGenres(element));
        
        Maybe<String> image = getImage(element);
        if (image.hasValue()) {
            brand.setImage(image.requireValue());
        }
        
        Specialization specialization = specializationFrom(element);
        
        brand.setMediaType(MediaType.VIDEO);
        brand.setSpecialization(specialization);
        
        EpisodeProcessingNodeFactory nodeFactory = new EpisodeProcessingNodeFactory(episodeProcessor, specialization);
        try {
        	    String responseBody = httpClient.get(getShowUri(id) + WATCHABLES_URL_SUFFIX).body();
            new Builder(nodeFactory).build(new StringReader(responseBody));
        } catch(Exception e) {
            log.error("Exception parsing episodes for brand " + brand.getTitle(), e);
            return;
        }
        
        if(specialization == FILM && nodeFactory.items.size() == 1) {
            setFilmDescription((Film)Iterables.getOnlyElement(nodeFactory.items), element);
        }
        
        writer.createOrUpdate(brand);
        for (Series series : episodeProcessor.getSeriesMap().values()) {
            writer.createOrUpdate(series);
        }
        for (Item item : nodeFactory.items) {
            item.setContainer(brand);
        	writer.createOrUpdate(item);
        }
    }
    
    private static final Pattern FILM_YEAR = Pattern.compile(".*\\((\\d{4})\\)$");
    
    private void setFilmDescription(Film film, Element element) {
        Maybe<String> description = getDescription(element);
        if(description.hasValue()) {
            film.setDescription(description.requireValue());
        }
        String shortDesc = childValue(element, "short_description");
        if(!Strings.isNullOrEmpty(shortDesc)) {
            Matcher matcher = FILM_YEAR.matcher(shortDesc);
            if(matcher.matches()) {
                film.setYear(Integer.parseInt(matcher.group(1)));
            }
        }
    }

    private Specialization specializationFrom(Element element) {
        String progType = childValue(element, "programme_type");
        if(progType.equals("Feature Film") || progType.equals("TV Movie")) {
            return Specialization.FILM;
        }
        return Specialization.TV;
    }

    private String getShowUri(String id) {
        return baseApiUrl + "/shows/" + id;
    }
    
    private String getBrandCurie(String id) {
        return "five:b-" + id;
    }
    
    private String childValue(Element element, String childName) {
        Element firstChild = element.getFirstChildElement(childName);
        if(firstChild != null) {
            return firstChild.getValue();
        }
        return null;
    }
    
    private Maybe<String> getDescription(Element element) {
        String longDescription = element.getFirstChildElement("long_description").getValue();
        if (!Strings.isNullOrEmpty(longDescription)) {
            return Maybe.just(longDescription);
        }
        
        String shortDescription = element.getFirstChildElement("short_description").getValue();
        if (!Strings.isNullOrEmpty(shortDescription)) {
            return Maybe.just(shortDescription);
        }
        
        return Maybe.nothing();
    }
    
    private Set<String> getGenres(Element element) {
        return genreMap.mapRecognised(ImmutableSet.of("http://www.five.tv/genres/" + element.getFirstChildElement("genre").getValue()));
    }
    
    private Maybe<String> getImage(Element element) {
        Elements imageElements = element.getFirstChildElement("images").getChildElements("image");
        if (imageElements.size() > 0) {
            return Maybe.just(imageElements.get(0).getValue());
        }
        
        return Maybe.nothing();
    }
    
    private class EpisodeProcessingNodeFactory extends NodeFactory {
        
        private final FiveEpisodeProcessor episodeProcessor;
		private final List<Item> items = Lists.newArrayList();
        private final Specialization specialization;

        public EpisodeProcessingNodeFactory(FiveEpisodeProcessor episodeProcessor, Specialization specialization) {
			this.episodeProcessor = episodeProcessor;
            this.specialization = specialization;
        }
        
        @Override
        public Nodes finishMakingElement(Element element) {
            if (element.getLocalName().equalsIgnoreCase("watchable")) {
                try {
                    items.add(episodeProcessor.processEpisode(element, specialization));
                }
                catch (Exception e) {
                    log.error("Exception when processing episode", e);
                }
                
                return new Nodes();
            }
            else {
                return super.finishMakingElement(element);
            }
        }
    }
}
