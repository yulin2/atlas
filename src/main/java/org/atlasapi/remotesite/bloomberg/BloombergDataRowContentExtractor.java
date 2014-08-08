package org.atlasapi.remotesite.bloomberg;

import static org.atlasapi.media.entity.Publisher.BLOOMBERG;

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
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.ImmutableList.Builder;

public class BloombergDataRowContentExtractor implements ContentExtractor<BloombergDataRow, Content> {

    private static final String BLOOMBERG_URI_PATTERN = "http://bloomberg.com/%s";
    
    private final Splitter idSplitter = Splitter.on(":").omitEmptyStrings();
    private final PeriodFormatter durationFormatter = new PeriodFormatterBuilder()
        .appendHours().minimumPrintedDigits(2)
        .appendSeparator(":")
        .appendMinutes().minimumPrintedDigits(2)
        .appendSeparator(":")
        .appendSeconds().minimumPrintedDigits(2)
        .toFormatter();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    
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
        item.setLastUpdated(new DateTime(DateTimeZone.UTC));
        item.setMediaType(MediaType.VIDEO);
        item.setKeyPhrases(keyphrases(source.getKeywords()));
        
        return item;
    }
    
    private Iterable<KeyPhrase> keyphrases(List<String> keywords) {
        Builder<KeyPhrase> keyphrases = new ImmutableList.Builder<KeyPhrase>();
        for (String keyword : keywords) {
            keyphrases.add(new KeyPhrase(keyword, BLOOMBERG));
        }
        return keyphrases.build();
    }
    
    private Set<Version> extractVersions(String duration) {
        Version version = new Version();
        version.setDuration(extractDuration(duration));
        return ImmutableSet.of(version);
    }

    private Duration extractDuration(String duration) {
        //duration is of type hh:mm:ss
        return durationFormatter.parsePeriod(duration).toStandardDuration();
    }

    private DateTime extractDate(String date) {
        return dateTimeFormatter.parseDateTime(date).withZone(DateTimeZone.UTC);
    }

    private String uri(String id) {
        return String.format(BLOOMBERG_URI_PATTERN, Iterables.getLast(idSplitter.split(id)));
    }
    
    private String curie(String id) {
        String curie = String.format(BLOOMBERG_URI_PATTERN, id);
        return curie;
    }
    
}
