package org.atlasapi.remotesite.channel4;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
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
    
    private final DateTimeFormatter fmt = DateTimeFormat.forPattern("EEEE dd MMMM yyyy hh:mm aa").withZone(DateTimeZones.LONDON).withLocale(Locale.UK);
	private final AdapterLog log;

    public C4EpisodeBroadcastExtractor(AdapterLog log) {
		this.log = log;
	}
    
    @SuppressWarnings("unchecked")
    @Override
    public List<Episode> extract(Feed source) {
        List<Episode> episodes = Lists.newArrayList();

        for (Entry entry : (List<Entry>) source.getEntries()) {
            Map<String, String> lookup = C4AtomApi.foreignElementLookup(entry);
            
            Integer seriesNumber = C4AtomApi.readAsNumber(lookup, C4EpisodesExtractor.DC_SERIES_NUMBER);
            Integer episodeNumber = C4AtomApi.readAsNumber(lookup, C4EpisodesExtractor.DC_EPISODE_NUMBER);
            
            String itemUri = C4AtomApi.canonicalUri(entry);
            
            if (itemUri == null) {
            	log.record(new AdapterLogEntry(Severity.WARN).withDescription("Could not find cannonical uri from epg entry " + entry.getId()));
            	continue;
            }
            
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
                Broadcast broadcast = C4BroadcastBuilder.broadcast().withChannel(txChannel).withTransmissionStart(txStart).withDuration(duration).withAtomId(entry.getId()).build();
                broadcast.setLastUpdated(new DateTime(entry.getUpdated(), DateTimeZones.UTC));
                version.addBroadcast(broadcast);
                episodes.add(episode);
            }
        }

        return episodes;
    }
}
