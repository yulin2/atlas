package org.atlasapi.remotesite.channel4.pmlsd.epg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Policy.RevenueContract;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.channel4.pmlsd.C4AtomApi;
import org.atlasapi.remotesite.channel4.pmlsd.ContentFactory;
import org.atlasapi.remotesite.channel4.pmlsd.epg.model.C4EpgEntry;
import org.joda.time.DateTime;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.metabroadcast.common.time.Clock;

public class C4EpgEntryItemExtractor implements ContentExtractor<C4EpgEntryItemSource, Item> {

    private final C4EpgEntryBroadcastExtractor broadcastExtractor = new C4EpgEntryBroadcastExtractor();
    private final Clock clock;
    private final ContentFactory<C4EpgEntry, C4EpgEntry, C4EpgEntry> contentFactory;
    
    public C4EpgEntryItemExtractor(ContentFactory<C4EpgEntry, C4EpgEntry, C4EpgEntry> contentFactory, Clock clock) {
        this.contentFactory = contentFactory;
        this.clock = clock;
    }
    
    @Override
    public Item extract(C4EpgEntryItemSource source) {
        C4EpgEntry entry = source.getEntry().getEpgEntry();

        Item item;
        if (source.getBrand().isPresent()) {
            Episode episode = contentFactory.createEpisode(entry).get();
            episode.setEpisodeNumber(episodeNumberFrom(source));
            episode.setSeriesNumber(seriesNumberFrom(source));
            item = episode;
        } else {
            item = contentFactory.createItem(entry).get();
        }
        
        DateTime now = clock.now();
        
        item.setLastUpdated(now);
        
        //TODO aliases
        item.addAliasUrl(C4AtomApi.hierarchyUriFromCanonical(item.getCanonicalUri()));
        
        item.setTitle(entry.title());
        item.setDescription(entry.summary());
        item.setMediaType(MediaType.VIDEO);
        item.setSpecialization(Specialization.TV);
        
        if (entry.media() != null && !Strings.isNullOrEmpty(entry.media().thumbnail())) {
            C4AtomApi.addImages(item, entry.media().thumbnail());
        }
        
        item.addVersion(extractVersion(source.getEntry(), now));
        
        return item;
    }

    private Version extractVersion(C4EpgChannelEntry entry, DateTime now) {
        Version version = new Version();
        version.setLastUpdated(now);
        
        version.setDuration(entry.getEpgEntry().duration());
        
        Broadcast broadcast = broadcastExtractor.extract(entry);
        broadcast.setLastUpdated(now);
        version.addBroadcast(broadcast);
        
        if (entry.getEpgEntry().media() != null && !Strings.isNullOrEmpty(entry.getEpgEntry().media().player())) {
            version.addManifestedAs(extractEncoding(entry, now));
        }
        
        return version;
    }

    private Encoding extractEncoding(C4EpgChannelEntry entry, DateTime now) {
        Encoding encoding = new Encoding();
        encoding.setLastUpdated(now);

        encoding.addAvailableAt(extractLocation(entry.getEpgEntry(), now));
        
        return encoding;
    }

    private Location extractLocation(C4EpgEntry entry, DateTime now) {
        Location location = new Location();
        location.setLastUpdated(now);
        location.setUri(entry.media().player());
        location.setTransportType(TransportType.LINK);
        location.setPolicy(policyFrom(entry));
        return location;
    }

    private static final Pattern AVAILABILTY_RANGE_PATTERN = Pattern.compile("start=(.*); end=(.*); scheme=W3C-DTF");

    private Policy policyFrom(C4EpgEntry entry) {
        Policy policy = new Policy();
        policy.setRevenueContract(RevenueContract.FREE_TO_VIEW);
        
        if(!Objects.equal(policy.getAvailableCountries(), entry.media().availableCountries())) {
            policy.setAvailableCountries(entry.media().availableCountries());
        }

        if (entry.available() != null) {
            Matcher matcher = AVAILABILTY_RANGE_PATTERN.matcher(entry.available());
            if (matcher.matches()) {
                DateTime availabilityStart = new DateTime(matcher.group(1));
                if (!Objects.equal(policy.getAvailabilityStart(), availabilityStart)) {
                    policy.setAvailabilityStart(availabilityStart);
                }
                DateTime availabilityEnd = new DateTime(matcher.group(2));
                if (!Objects.equal(policy.getAvailabilityEnd(), availabilityEnd)) {
                    policy.setAvailabilityEnd(availabilityEnd);
                }
            }
        }
        policy.setLastUpdated(entry.updated());
        return policy;
    }

    private Integer seriesNumberFrom(C4EpgEntryItemSource source) {
        C4EpgEntry epgEntry = source.getEntry().getEpgEntry();
        if (epgEntry.seriesNumber() != null) {
            return epgEntry.seriesNumber();
        }
        if (source.getSeries().isPresent() && source.getSeries().get().getSeriesNumber() != null) {
            return source.getSeries().get().getSeriesNumber();
        }
        return null;
    }

    private Integer episodeNumberFrom(C4EpgEntryItemSource source) {
        C4EpgEntry epgEntry = source.getEntry().getEpgEntry();
        return epgEntry.episodeNumber();
    }

}
