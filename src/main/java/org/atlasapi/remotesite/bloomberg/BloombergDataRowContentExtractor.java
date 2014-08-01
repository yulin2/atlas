package org.atlasapi.remotesite.bloomberg;

import static org.atlasapi.media.entity.Publisher.BLOOMBERG;

import java.util.Set;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.ContentExtractor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import com.google.common.collect.ImmutableSet;

public class BloombergDataRowContentExtractor implements ContentExtractor<BloombergDataRow, Content> {

    private static final String BLOOMBERG_URI_PATTERN = "http://bloomberg.com/%s";
    
    @Override
    public Content extract(BloombergDataRow source) {
        return extractItem(source);
    }

    private Item extractItem(BloombergDataRow source) {
        Item item = new Item();
        
        item.setVersions(extractVersions(source.getDuration()));
        item.setFirstSeen(extractDate(source.getDate()));
        item.setDescription(source.getDescription());
        item.setTitle(source.getTitle());
        item.setPublisher(BLOOMBERG);
        item.setCanonicalUri(uri(source.getId()));
        item.setCurie(curie(source.getId()));
        item.setLastUpdated(new DateTime().withZone(DateTimeZone.UTC));
        
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
        return String.format(BLOOMBERG_URI_PATTERN, id);
    }
    
    private String curie(String id) {
        String curie = String.format(BLOOMBERG_URI_PATTERN, id);
        return curie;
    }
    
}
