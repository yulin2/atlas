package org.atlasapi.remotesite.five;

import java.io.StringReader;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.NodeFactory;
import nu.xom.Nodes;

import org.atlasapi.genres.GenreMap;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.system.RemoteSiteClient;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.http.HttpResponse;

public class FiveBrandProcessor {
    
    private final static String WATCHABLES_URL_SUFFIX = "/watchables?expand=season%7Ctransmissions";
    private final ContentWriter writer;
    private final GenreMap genreMap = new FiveGenreMap();
    private final AdapterLog log;
    private final FiveEpisodeProcessor episodeProcessor;
    private final String baseApiUrl;
    private final RemoteSiteClient<HttpResponse> httpClient;

    public FiveBrandProcessor(ContentWriter writer, AdapterLog log, String baseApiUrl, RemoteSiteClient<HttpResponse> httpClient) {
        this.writer = writer;
        this.log = log;
        this.baseApiUrl = baseApiUrl;
        this.httpClient = httpClient;
        this.episodeProcessor = new FiveEpisodeProcessor(baseApiUrl, httpClient);
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
        
        Builder builder = new Builder(new EpisodeProcessingNodeFactory(brand, episodeProcessor));
        try {
            builder.build(new StringReader(httpClient.get(getShowUri(id) + WATCHABLES_URL_SUFFIX).body()));
            
            writer.createOrUpdate(brand, true);
        }
        catch(Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(getClass()).withDescription("Exception while trying to parse episodes for brand " + brand.getTitle()));
        }
    }
    
    private String getShowUri(String id) {
        return baseApiUrl + "/shows/" + id;
    }
    
    private String getBrandCurie(String id) {
        return "five:b-" + id;
    }
    
    private String childValue(Element element, String childName) {
        return element.getFirstChildElement(childName).getValue();
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
        
        private final Brand brand;
        private final FiveEpisodeProcessor episodeProcessor;

        public EpisodeProcessingNodeFactory(Brand brand, FiveEpisodeProcessor episodeProcessor) {
            this.brand = brand;
            this.episodeProcessor = episodeProcessor;
        }
        
        @Override
        public Nodes finishMakingElement(Element element) {
            if (element.getLocalName().equalsIgnoreCase("watchable")) {
                try {
                    brand.addContents(episodeProcessor.processEpisode(element));
                }
                catch (Exception e) {
                    log.record(new AdapterLogEntry(Severity.ERROR).withSource(FiveEpisodeProcessor.class).withCause(e).withDescription("Exception when processing episode"));
                }
                
                return new Nodes();
            }
            else {
                return super.finishMakingElement(element);
            }
        }
    }
}
