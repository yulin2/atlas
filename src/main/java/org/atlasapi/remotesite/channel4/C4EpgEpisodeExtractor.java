package org.atlasapi.remotesite.channel4;

import java.util.Map;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.ContentExtractor;
import org.jdom.Element;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.DateTimeZones;
import com.sun.syndication.feed.atom.Entry;

public class C4EpgEpisodeExtractor extends BaseC4EpisodeExtractor implements
        ContentExtractor<Entry, Episode> {
    
    private static final String DC_START_TIME = "dc:relation.TXStartTime";
    private static final String DC_TX_CHANNEL = "dc:relation.TXChannel";

    private static final DateTimeFormatter fmt = DateTimeFormat.forPattern("EEEE dd MMMM yyyy hh:mm aa").withZone(DateTimeZones.LONDON);
    private final Map<String, String> channelLookup;

    public C4EpgEpisodeExtractor(C4AtomApi atomApi, Clock clock) {
        super(clock);
        this.channelLookup = ImmutableMap.copyOf(
                Maps.transformValues(atomApi.getChannelMap(), Identified.TO_URI));
    }

    @Override // epg.atom entries don't have media groups/content, so no images.
    protected Element getMedia(Entry source) {
        return null;
    }

    @Override
    public Episode extract(Entry source) {

        Map<String, String> lookup = C4AtomApi.foreignElementLookup(source);

        Episode episode = createBasicEpisode(source, lookup);

        Version version = new Version();
        episode.setVersions(Sets.newHashSet(version));

        String channelKey = lookup.get(DC_TX_CHANNEL);
        String txChannel = channelLookup.get(channelKey);
        String startTime = lookup.get(DC_START_TIME);
        Duration duration = C4AtomApi.durationFrom(lookup);

        if (txChannel != null) {
            DateTime txStart = fmt.parseDateTime(startTime);
            Broadcast broadcast = C4BroadcastBuilder.broadcast()
                    .withChannel(txChannel)
                    .withTransmissionStart(txStart)
                    .withDuration(duration)
                    .withAtomId(source.getId())
                    .build();
            
            broadcast.setLastUpdated(episode.getLastUpdated());
            version.addBroadcast(broadcast);
        } else {
            throw new IllegalArgumentException(String.format("Channel %s not recognised, key was %s", txChannel, channelKey));
        }

        return episode;
    }

    @Override
    protected String extractEpisodeUri(Entry source, Map<String, String> lookup) {
        return C4AtomApi.canonicalUri(source);
    }

}
