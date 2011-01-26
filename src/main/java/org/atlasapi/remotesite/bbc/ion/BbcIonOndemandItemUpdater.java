package org.atlasapi.remotesite.bbc.ion;

import java.util.Set;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Countries;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.bbc.BbcProgrammeGraphExtractor;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChange;
import org.joda.time.Interval;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.SystemClock;

public class BbcIonOndemandItemUpdater {
    
    private final Clock clock = new SystemClock();
    
    public void updateItemDetails(Item item, IonOndemandChange change) {
        boolean revoked = "revoked".equals(change.getRevocationStatus());
        Maybe<Version> version = version(item.getVersions(), BbcIonOndemandChangeUpdater.SLASH_PROGRAMMES_BASE+change.getVersionId());
        
        if (version.hasValue()) {
            processVersion(change, revoked, version.requireValue());
        } else if (! revoked) {
            version = versionWithBadUri(item.getVersions(), BbcIonOndemandChangeUpdater.SLASH_PROGRAMMES_BASE+change.getEpisodeId());
            if (version.hasValue()) {
                version.requireValue().setCanonicalUri(BbcIonOndemandChangeUpdater.SLASH_PROGRAMMES_BASE+change.getVersionId());
                processVersion(change, revoked, version.requireValue());
            }
            
            Version newVersion = new Version();
            newVersion.setCanonicalUri(BbcIonOndemandChangeUpdater.SLASH_PROGRAMMES_BASE+change.getVersionId());
            Encoding newEncoding = new Encoding();
            Location newLocation = createLocation(change);
            newEncoding.addAvailableAt(newLocation);
            newVersion.addManifestedAs(newEncoding);
            item.addVersion(newVersion);
        }
    }

    private void processVersion(IonOndemandChange change, boolean revoked, Version version) {
        Maybe<Encoding> encoding = encoding(version.getManifestedAs());
        if (encoding.hasValue()) {
            Maybe<Location> location = location(encoding.requireValue().getAvailableAt(), BbcProgrammeGraphExtractor.iplayerPageFrom(BbcIonOndemandChangeUpdater.SLASH_PROGRAMMES_BASE+change.getEpisodeId()));
            if (location.hasValue()) {
                if (! revoked) {
                    mergeLocation(location.requireValue(), change);
                } else {
                    removeLocation(encoding.requireValue(), location.requireValue());
                }
            } else if (! revoked) {
                Location newLocation = createLocation(change);
                encoding.requireValue().addAvailableAt(newLocation);
            }
        } else if (! revoked) {
            Encoding newEncoding = new Encoding();
            Location newLocation = createLocation(change);
            newEncoding.addAvailableAt(newLocation);
            version.addManifestedAs(newEncoding);
        }
    }
    
    private Maybe<Version> versionWithBadUri(Set<Version> versions, String locationUri) {
        for (Version version: versions) {
            for (Encoding encoding: version.getManifestedAs()) {
                for (Location location: encoding.getAvailableAt()) {
                    if (locationUri.equals(location.getUri())) {
                        return Maybe.just(version);
                    }
                }
            }
        }
        return Maybe.nothing();
    }
    
    private void removeLocation(Encoding encoding, Location location) {
        if (location.getUri() != null) {
            Set<Location> locations = Sets.newHashSet();
            
            for (Location loc: encoding.getAvailableAt()) {
                if (! loc.getUri().equals(location.getUri())) {
                    locations.add(loc);
                }
            }
            
            encoding.setAvailableAt(locations);
        }
    }
    
    private Maybe<Version> version(Set<Version> versions, String versionId) {
        for (Version version: versions) {
            if (versionId.equals(version.getCanonicalUri())) {
                return Maybe.just(version);
            }
        }
        return Maybe.nothing();
    }
    
    private Maybe<Location> location(Set<Location> locations, String uri) {
        for (Location location: locations) {
            if (uri.equals(location.getUri())) {
                return Maybe.just(location);
            }
        }
        return Maybe.nothing();
    }
    
    private Location createLocation(IonOndemandChange change) {
        Location location = new Location();
        location.setUri(BbcProgrammeGraphExtractor.iplayerPageFrom(BbcIonOndemandChangeUpdater.SLASH_PROGRAMMES_BASE+change.getEpisodeId()));
        location.setTransportType(TransportType.LINK);
        
        Policy policy = new Policy();
        policy.setAvailableCountries(ImmutableSet.of(Countries.GB));
        policy.setAvailabilityStart(change.getScheduledStart());
        policy.setAvailabilityEnd(change.getDiscoverableEnd());
        location.setPolicy(policy);

        location.setAvailable(availableNow(policy));
        return location;
    }
    
    private void mergeLocation(Location location, IonOndemandChange change) {
        location.setUri(BbcProgrammeGraphExtractor.iplayerPageFrom(BbcIonOndemandChangeUpdater.SLASH_PROGRAMMES_BASE+change.getEpisodeId()));
        location.setTransportType(TransportType.LINK);
        
        Policy policy = location.getPolicy();
        if (policy == null) {
            policy = new Policy();
            policy.setAvailableCountries(ImmutableSet.of(Countries.GB));
            location.setPolicy(policy);
        }
        policy.setAvailabilityStart(change.getScheduledStart());
        policy.setAvailabilityEnd(change.getDiscoverableEnd());

        location.setAvailable(availableNow(policy));
    }
    
    private boolean availableNow(Policy policy) {
        return new Interval(policy.getAvailabilityStart(), policy.getAvailabilityEnd()).contains(clock.now());
    }
    
    private Maybe<Encoding> encoding(Set<Encoding> encodings) {
        for (Encoding encoding: encodings) {
            return Maybe.just(encoding);
        }
        return Maybe.nothing();
    }
}
