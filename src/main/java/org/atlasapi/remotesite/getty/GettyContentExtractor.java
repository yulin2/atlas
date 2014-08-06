package org.atlasapi.remotesite.getty;

import static org.atlasapi.media.entity.Publisher.GETTY;

import java.util.Set;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.ContentExtractor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import com.google.common.collect.ImmutableSet;

public class GettyContentExtractor implements ContentExtractor<VideoResponse, Content> {

    private static final String GETTY_URI_PATTERN = "http://gettyimages.co.uk/%s";
    
    @Override
    public Content extract(VideoResponse source) {
        return extractItem(source);
    }

    private Item extractItem(VideoResponse source) {
        Item item = new Item();
        
        item.setVersions(extractVersions(source.getDuration()));
        item.setFirstSeen(extractDate(source.getDateCreated()));
        item.setDescription(source.getDescription());
        item.setTitle(source.getTitle());
        item.setPublisher(GETTY);
        item.setCanonicalUri(uri(source.getAssetId()));
        item.setCurie(curie(source.getAssetId()));
        item.setLastUpdated(new DateTime(DateTimeZone.UTC));
        
        return item;
    }
    
    private Set<Version> extractVersions(String duration) {
        Version version = new Version();
        version.setDuration(extractDuration(duration));
        return ImmutableSet.of(version);
    }

    private Duration extractDuration(String duration) {
        return Duration.standardSeconds(0);
    }

    private DateTime extractDate(String date) {
        return new DateTime().withZone(DateTimeZone.UTC);
    }

    private String uri(String id) {
        return String.format(GETTY_URI_PATTERN, id);
    }
    
    private String curie(String id) {
        String curie = String.format(GETTY_URI_PATTERN, id);
        return curie;
    }

}
