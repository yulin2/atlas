package org.atlasapi.remotesite.channel4.pmlsd.epg;

import java.util.Set;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Version;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class C4SynthesizedItemMerger {

    public void merge(Episode synthesized, Episode canonical) {
        if (synthesized == null || canonical == null) {
            return; // lucky us.
        }

        Version version = Iterables.getOnlyElement(canonical.getVersions());
        Version synthVersion = Iterables.getOnlyElement(synthesized.getVersions());

        mergeBroadcasts(version, synthVersion);
        mergeLocations(version, synthVersion);
    }

    private void mergeLocations(Version version, Version synthVersion) {
        Encoding encoding = Iterables.getOnlyElement(version.getManifestedAs());
        Encoding synthEncoding = Iterables.getOnlyElement(synthVersion.getManifestedAs());

        /* create a set of the uris of the current locations in the canon episode.
         * copy in locations from synth episode if they're not in the uri set.
         */
        ImmutableSet<String> locationUris = currentLocationUris(encoding.getAvailableAt());
        for (Location location : synthEncoding.getAvailableAt()) {
            if (!locationUris.contains(location.getUri())) {
                encoding.addAvailableAt(location);
            }
        }
    }

    private ImmutableSet<String> currentLocationUris(Set<Location> locations) {
        return ImmutableSet.copyOf(Iterables.transform(locations, new Function<Location, String>() {
            @Override
            public String apply(Location location) {
                return location.getUri();
            }
        }));
    }

    private void mergeBroadcasts(Version version, Version synthVersion) {
        /* Create a set of current broadcast ids in .
         * Copy in broadcasts from synth if not in canon.
         */
        Set<String> currentBroadcastIds = currentBroadcastIds(version.getBroadcasts());
        for (Broadcast synthBroadcast : synthVersion.getBroadcasts()) {
            if (!currentBroadcastIds.contains(synthBroadcast.getSourceId())) {
                version.addBroadcast(synthBroadcast);
            }
        }
    }

    private ImmutableSet<String> currentBroadcastIds(Set<Broadcast> broadcasts) {
        return ImmutableSet.copyOf(Iterables.transform(broadcasts, new Function<Broadcast, String>() {
            @Override
            public String apply(Broadcast broadcast) {
                return broadcast.getSourceId();
            }
        }));
    }

}
