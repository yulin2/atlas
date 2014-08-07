package org.atlasapi.remotesite.globalimageworks;

import static org.atlasapi.media.entity.Publisher.GLOBALIMAGEWORKS;

import java.util.List;
import java.util.Set;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.KeyPhrase;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.ContentExtractor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class GlobalImageDataRowContentExtractor implements ContentExtractor<GlobalImageDataRow, Content> {

    private static final String GLOBAL_IMAGE_URI_PATTERN = "http://globalimageworks.com/%s";
    private static final int SECONDS_PER_MIN = 60;
    private static final int SECONDS_PER_HOUR = 3660;
    
    private final Splitter idSplitter = Splitter.on(":").omitEmptyStrings();
    private final Splitter frameSplitter = Splitter.on(";").omitEmptyStrings();
    private final Splitter durationSplitter = Splitter.on(":").omitEmptyStrings();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    
    @Override
    public Content extract(GlobalImageDataRow source) {
        return extractItem(source);
    }

    private Item extractItem(GlobalImageDataRow source) {
        Item item = new Item();
        
        item.setVersions(extractVersions(source.getDuration()));
        item.setFirstSeen(extractDate(source.getDate()));
        item.setDescription(source.getDescription());
        item.setTitle(source.getTitle());
        item.setPublisher(GLOBALIMAGEWORKS);
        item.setCanonicalUri(uri(source.getId()));
        item.setCurie(curie(source.getId()));
        item.setLastUpdated(new DateTime(DateTimeZone.UTC));
        item.setMediaType(MediaType.VIDEO);
        item.setKeyPhrases(keyphrases(source.getKeywords()));
        
        return item;
    }
    
    private Iterable<KeyPhrase> keyphrases(List<String> keywords) {
        Builder<KeyPhrase> keyphrases = new ImmutableList.Builder<KeyPhrase>();
        for (String keyword : keywords) {
            keyphrases.add(new KeyPhrase(keyword, GLOBALIMAGEWORKS));
        }
        return keyphrases.build();
    }

    private Set<Version> extractVersions(String duration) {
        Version version = new Version();
        version.setDuration(extractDuration(duration));
        return ImmutableSet.of(version);
    }

    private Duration extractDuration(String duration) {
        //duration is of type hh:mm:ss;f
        List<String> tokens = ImmutableList.copyOf(durationSplitter.split(duration));
        int hours = Integer.parseInt(tokens.get(0));
        int minutes = Integer.parseInt(tokens.get(1));
        int seconds = Integer.parseInt(frameSplitter.split(tokens.get(2)).iterator().next());
        return Duration.standardSeconds(seconds + minutes * SECONDS_PER_MIN + hours * SECONDS_PER_HOUR);
    }

    private DateTime extractDate(String date) {
        return dateTimeFormatter.parseDateTime(date).withZone(DateTimeZone.UTC);
    }

    private String uri(String id) {
        return String.format(GLOBAL_IMAGE_URI_PATTERN, Iterables.getLast(idSplitter.split(id)));
    }
    
    private String curie(String id) {
        String curie = String.format(GLOBAL_IMAGE_URI_PATTERN, id);
        return curie;
    }

}
