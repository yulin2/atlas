package org.atlasapi.remotesite.channel4;

import java.util.List;
import java.util.Map;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.query.content.PerPublisherCurieExpander;
import org.atlasapi.remotesite.ContentExtractor;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.metabroadcast.common.time.DateTimeZones;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;

public class C4EpisodeBroadcastExtractor implements ContentExtractor<Feed, List<Episode>> {
    
    private final DateTimeFormatter fmt = DateTimeFormat.forPattern("EEEE dd MMMM yyyy hh:mm aa").withZone(DateTimeZones.LONDON);

    @SuppressWarnings("unchecked")
    @Override
    public List<Episode> extract(Feed source) {
        List<Episode> episodes = Lists.newArrayList();

        for (Entry entry : (List<Entry>) source.getEntries()) {
            Map<String, String> lookup = C4AtomApi.foreignElementLookup(entry);
            
            Integer seriesNumber = C4AtomApi.readAsNumber(lookup, C4EpisodesExtractor.DC_SERIES_NUMBER);
            Integer episodeNumber = C4AtomApi.readAsNumber(lookup, C4EpisodesExtractor.DC_EPISODE_NUMBER);
            
            String itemUri = C4AtomApi.canonicalUri(entry);
            
            Episode episode = new Episode(itemUri, PerPublisherCurieExpander.CurieAlgorithm.C4.compact(itemUri), Publisher.C4);
            episode.setEpisodeNumber(episodeNumber);
            episode.setSeriesNumber(seriesNumber);
            
            Version version = new Version();
            episode.setVersions(Sets.newHashSet(version));
            
            String txChannel = C4EpisodesExtractor.CHANNEL_LOOKUP.get(lookup.get(C4EpisodesExtractor.DC_TX_CHANNEL));
            String startTime = lookup.get(C4EpisodesExtractor.DC_START_TIME);
            Duration duration = C4AtomApi.durationFrom(lookup);
            
            if (txChannel != null) {
                DateTime txStart = fmt.parseDateTime(startTime);
                Broadcast broadcast = new Broadcast(txChannel, txStart, duration);
                broadcast.addAlias(entry.getId());
                broadcast.setLastUpdated(new DateTime(entry.getUpdated(), DateTimeZones.UTC));
                version.addBroadcast(broadcast);
                episodes.add(episode);
            }
        }

        return episodes;
    }

}
