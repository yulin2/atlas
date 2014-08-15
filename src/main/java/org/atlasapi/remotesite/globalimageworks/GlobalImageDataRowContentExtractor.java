package org.atlasapi.remotesite.globalimageworks;

import static org.atlasapi.media.entity.Publisher.GLOBALIMAGEWORKS;

import java.util.List;
import java.util.Set;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.KeyPhrase;
import org.atlasapi.media.entity.Location;
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
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class GlobalImageDataRowContentExtractor implements ContentExtractor<GlobalImageDataRow, Content> {

    private static final String GLOBAL_IMAGE_URI_PATTERN = "http://globalimageworks.com/%s";
    private static final String GLOBAL_IMAGE_CURIE_PATTERN = "globalImageWorks:%s";
    
    private final Splitter idSplitter = Splitter.on(":").omitEmptyStrings();
    private final PeriodFormatter durationFormatter = new PeriodFormatterBuilder()
        .appendHours().minimumPrintedDigits(2)
        .appendSeparator(":")
        .appendMinutes().minimumPrintedDigits(2)
        .appendSeparator(":")
        .appendSeconds().minimumPrintedDigits(2)
        .appendSeparator(";")
        .appendMillis().minimumPrintedDigits(2)
        .toFormatter();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
    
    @Override
    public Content extract(GlobalImageDataRow source) {
        return extractItem(source);
    }

    private Item extractItem(GlobalImageDataRow source) {
        Item item = new Item();
        
        String id = Iterables.getLast(idSplitter.split(source.getId()));
        
        item.setVersions(extractVersions(source.getDuration()));
        item.setFirstSeen(extractDate(source.getDate()));
        item.setDescription(source.getDescription());
        item.setTitle(source.getTitle());
        item.setPublisher(GLOBALIMAGEWORKS);
        item.setCanonicalUri(uri(id));
        item.setCurie(curie(id));
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
        Encoding encoding = new Encoding();
        encoding.setAvailableAt(ImmutableSet.of(new Location()));
        version.addManifestedAs(encoding);
        version.setDuration(extractDuration(duration));
        return ImmutableSet.of(version);
    }

    private Duration extractDuration(String duration) {
        //duration is of type hh:mm:ss;f
        return durationFormatter.parsePeriod(duration).toStandardDuration();
    }

    private DateTime extractDate(String date) {
        return dateTimeFormatter.parseDateTime(date).withZone(DateTimeZone.UTC);
    }

    private String uri(String id) {
        return String.format(GLOBAL_IMAGE_URI_PATTERN, id);
    }
    
    private String curie(String id) {
        String curie = String.format(GLOBAL_IMAGE_CURIE_PATTERN, id);
        return curie;
    }

}
