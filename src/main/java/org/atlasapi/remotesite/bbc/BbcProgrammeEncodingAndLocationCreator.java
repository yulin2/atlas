package org.atlasapi.remotesite.bbc;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.remotesite.bbc.ion.IonService;
import org.atlasapi.remotesite.bbc.ion.IonService.MediaSetsToPoliciesFunction;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChange;
import org.joda.time.Interval;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.time.Clock;

public class BbcProgrammeEncodingAndLocationCreator {
    private static final String SLASH_PROGRAMMES_ROOT = "http://www.bbc.co.uk/programmes/";
    
    private final Clock clock;
    private final MediaSetsToPoliciesFunction mediaSetsToPoliciesFunction;
    
    
    public BbcProgrammeEncodingAndLocationCreator(MediaSetsToPoliciesFunction mediaSetsToPoliciesFunction, Clock clock) {
        this.mediaSetsToPoliciesFunction = checkNotNull(mediaSetsToPoliciesFunction);
        this.clock = checkNotNull(clock);
    }
    
    public Maybe<Encoding> createEncoding(IonOndemandChange ondemand) {
        return createEncoding(ondemand, ondemand.getEpisodeId());
    }

    public Maybe<Encoding> createEncoding(IonOndemandChange ondemand, String episodeId) {
        Preconditions.checkNotNull(episodeId);
        
        Maybe<IonService> ionService = IonService.fromString(ondemand.getService());
        if(ionService.hasValue()) {
            Encoding encoding = new Encoding();
            encoding.setCanonicalUri(SLASH_PROGRAMMES_ROOT+ondemand.getId());
            
            IonService requiredIonService = ionService.requireValue();
            requiredIonService.applyToEncoding(encoding, mediaSetsToPoliciesFunction);
            for (Location location : encoding.getAvailableAt()) {
                applyToLocation(location, ondemand, episodeId);
            }

            return Maybe.just(encoding);
        }
        return Maybe.nothing();
    }

    public List<Location> locations(IonOndemandChange change) {
        Maybe<IonService> ionService = IonService.fromString(change.getService());
        if (ionService.hasValue()) {
            List<Location> locations = ionService.requireValue().locations(mediaSetsToPoliciesFunction);
            for (Location location : locations) {
                applyToLocation(location, change, change.getEpisodeId());
            }
            return locations;
        }
        return ImmutableList.of();
    }

    private void applyToLocation(Location location, IonOndemandChange ondemand, String episodeId) {

        Policy policy = location.getPolicy();
        applyToPolicy(policy, ondemand);
        
        location.setAvailable(availableNow(policy));
        location.setCanonicalUri(SLASH_PROGRAMMES_ROOT + ondemand.getId());
        location.setTransportType(TransportType.LINK);
        location.setUri("http://www.bbc.co.uk/iplayer/episode/" + episodeId);
    }
    
    private void applyToPolicy(Policy policy, IonOndemandChange ondemand) {
        policy.setActualAvailabilityStart(ondemand.getActualStart());
        policy.setAvailabilityStart(ondemand.getScheduledStart());
        policy.setAvailabilityEnd(ondemand.getEnd() == null ? policy.getAvailabilityStart().plus(ondemand.getDuration()) : ondemand.getEnd());
    }

    private boolean availableNow(Policy policy) {
        if (policy.getAvailabilityStart() == null || policy.getAvailabilityEnd() == null || policy.getAvailabilityStart().isAfter(policy.getAvailabilityEnd())) {
            return false;
        }
        return new Interval(policy.getAvailabilityStart(), policy.getAvailabilityEnd()).contains(clock.now());
    }
}
