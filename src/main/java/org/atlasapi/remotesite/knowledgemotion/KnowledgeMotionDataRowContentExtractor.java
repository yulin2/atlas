package org.atlasapi.remotesite.knowledgemotion;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Set;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.KeyPhrase;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.knowledgemotion.topics.TopicGuesser;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class KnowledgeMotionDataRowContentExtractor implements ContentExtractor<KnowledgeMotionDataRow, Optional<? extends Content>> {

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

    private final ImmutableMap<String, KnowledgeMotionSourceConfig> sources;
    private final TopicGuesser topicGuesser;

    public KnowledgeMotionDataRowContentExtractor(Iterable<KnowledgeMotionSourceConfig> sources, TopicGuesser topicGuesser) {
        ImmutableMap.Builder<String, KnowledgeMotionSourceConfig> sourceMap = ImmutableMap.builder();
        for (KnowledgeMotionSourceConfig source : sources) {
            sourceMap.put(source.rowHeader(), source);
        }
        this.sources = sourceMap.build();
        this.topicGuesser = checkNotNull(topicGuesser);
    }

    @Override
    public Optional<? extends Content> extract(KnowledgeMotionDataRow source) {
        return extractItem(source);
    }

    private Optional<Item> extractItem(KnowledgeMotionDataRow dataRow) {
        KnowledgeMotionSourceConfig sourceConfig = sources.get(dataRow.getSource());
        if (sourceConfig == null) {
            return Optional.absent();
        }

        Item item = new Item();

        String id = Iterables.getLast(idSplitter.split(dataRow.getId()));
        Publisher publisher = sourceConfig.publisher();

        item.setVersions(extractVersions(dataRow.getDuration()));
        item.setFirstSeen(extractDate(dataRow.getDate()));
        item.setDescription(dataRow.getDescription());
        item.setTitle(dataRow.getTitle());
        item.setPublisher(publisher);
        item.setCanonicalUri(sourceConfig.uri(id));
        item.setCurie(sourceConfig.curie(id));
        item.setLastUpdated(new DateTime(DateTimeZone.UTC));
        item.setMediaType(MediaType.VIDEO);

        List<String> keyPhrases = dataRow.getKeywords();
        item.setKeyPhrases(keyphrases(keyPhrases, publisher));
        item.setTopicRefs(topicGuesser.guessTopics(keyPhrases));

        return Optional.of(item);
    }

    private Iterable<KeyPhrase> keyphrases(List<String> keywords, Publisher publisher) {
        Builder<KeyPhrase> keyphrases = new ImmutableList.Builder<KeyPhrase>();
        for (String keyword : keywords) {
            keyphrases.add(new KeyPhrase(keyword, publisher));
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

}
