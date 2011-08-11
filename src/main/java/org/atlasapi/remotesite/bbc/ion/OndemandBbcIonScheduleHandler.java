package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.Set;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
import org.joda.time.DateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.intl.Countries;

public class OndemandBbcIonScheduleHandler implements BbcIonScheduleHandler {

    private final ContentResolver resolver;
    private final ContentWriter writer;
    private final AdapterLog log;

    public OndemandBbcIonScheduleHandler(ContentResolver resolver, ContentWriter writer, AdapterLog log) {
        this.resolver = resolver;
        this.writer = writer;
        this.log = log;
    }

    @Override
    public int handle(IonSchedule schedule) {
        int broadcasts = 0;

        for (IonBroadcast broadcast : schedule.getBlocklist()) {
            try {
                if (handle(broadcast)) {
                    broadcasts++;
                }
            } catch (Exception e) {
                log.record(warnEntry().withSource(getClass()).withCause(e).withDescription("Failed to process ondemand for %s", broadcast.getEpisodeId()));
            }
        }
        return broadcasts;
    }

    private boolean handle(IonBroadcast broadcast) {

        String itemId = broadcast.getEpisodeId();

        Item item = resolve(BbcFeeds.slashProgrammesUriForPid(itemId));
        if (item == null) {
            log.record(warnEntry().withSource(getClass()).withDescription("No item %s", broadcast.getEpisodeId()));
            return false;
        }

        Version version = findVersion(item, BbcFeeds.slashProgrammesUriForPid(broadcast.getVersionId()));
        if (version == null) {
            log.record(warnEntry().withSource(getClass()).withDescription("No version %s for %s", broadcast.getVersionId(), broadcast.getEpisodeId()));
            return false;
        }

        String iplayerId = iplayerId(itemId);

        Encoding encoding = findEncoding(version, iplayerId);

        DateTime actualStart = broadcast.getEpisode().getActualStart();
        DateTime availableUntil = broadcast.getEpisode().getAvailableUntil();

        if ("CURRENT".equals(broadcast.getEpisode().getAvailability()) && actualStart != null && availableUntil != null) {
            Location location = null;

            if (encoding == null) {
                encoding = new Encoding();
                encoding.setCanonicalUri(iplayerId);
                version.addManifestedAs(encoding);

                location = new Location();
                encoding.addAvailableAt(location);
            }
            location = Iterables.getOnlyElement(encoding.getAvailableAt());


            updateLocation(location, actualStart, availableUntil, itemId);
        } else {
            if (encoding != null) {
                Set<Encoding> encodings = Sets.newHashSet(version.getManifestedAs());
                encodings.remove(encoding);
                version.setManifestedAs(encodings);
            }
        }

        writer.createOrUpdate(item);

        return true;
    }

    private void updateLocation(Location location, DateTime actualStart, DateTime availableUntil, String itemId) {
        Policy policy = new Policy();
        policy.setAvailabilityStart(actualStart);
        policy.setAvailabilityEnd(availableUntil);
        policy.setAvailableCountries(ImmutableSet.of(Countries.GB));
        location.setPolicy(policy);
        location.setCanonicalUri(iplayerId(itemId));

        location.setTransportType(TransportType.LINK);
        location.setUri("http://www.bbc.co.uk/iplayer/episode/" + itemId);
    }

    private Encoding findEncoding(Version version, String iplayerId) {
        for (Encoding encoding : version.getManifestedAs()) {
            if (iplayerId.equals(encoding.getCanonicalUri())) {
                return encoding;
            }
        }
        return null;
    }

    private String iplayerId(String itemId) {
        return String.format("iplayer:" + itemId);
    }

    private Version findVersion(Item item, String versionId) {
        for (Version version : item.getVersions()) {
            if (versionId.equals(version.getCanonicalUri())) {
                return version;
            }
        }
        log.record(warnEntry().withSource(getClass()).withDescription("No version %s for %s", versionId, item.getCanonicalUri()));
        return null;
    }

    private Item resolve(String uri) {
        Maybe<Identified> maybeItem = resolver.findByCanonicalUris(ImmutableList.of(uri)).get(uri);
        if (maybeItem.isNothing()) {
            log.record(warnEntry().withSource(getClass()).withDescription("Couldn't resolve item for %s", uri));
            return null;
        }
        Identified resolved = maybeItem.requireValue();
        if (!(resolved instanceof Item)) {
            log.record(warnEntry().withSource(getClass()).withDescription("Resolved %s not item for %s", resolved.getClass().getSimpleName(), uri));
            return null;
        }
        return (Item) resolved;
    }

}
