package org.atlasapi.remotesite.bbc;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.content.Encoding;
import org.atlasapi.media.content.Location;
import org.atlasapi.media.content.Policy;
import org.atlasapi.remotesite.bbc.ion.IonService;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChange;
import org.joda.time.Interval;

import com.google.common.base.Preconditions;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.time.Clock;

public class BbcProgrammeEncodingAndLocationCreator {
    private static final String SLASH_PROGRAMMES_ROOT = "http://www.bbc.co.uk/programmes/";
    private final Clock clock;
    
    
    public BbcProgrammeEncodingAndLocationCreator(Clock clock) {
        this.clock = clock;
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
            encoding.addAvailableAt(location(ondemand, episodeId));
            
            IonService requiredIonService = ionService.requireValue();
            requiredIonService.applyToEncoding(encoding);
            return Maybe.just(encoding);
        }
        return Maybe.nothing();
    }
    
    public Maybe<Location> location(IonOndemandChange ondemand) {
        Location location = location(ondemand, ondemand.getEpisodeId());
        Maybe<IonService> possibleService = IonService.fromString(ondemand.getService());
        if (possibleService.isNothing()) {
        	return Maybe.nothing();
        }
        possibleService.requireValue().applyToLocation(location);
        return Maybe.just(location);
    }

    private Location location(IonOndemandChange ondemand, String episodeId) {
        Location location = new Location();

        Policy policy = policyFrom(ondemand);
        location.setPolicy(policy);
        
        location.setAvailable(availableNow(policy));
        location.setCanonicalUri(SLASH_PROGRAMMES_ROOT + ondemand.getId());
        location.setTransportType(TransportType.LINK);
        location.setUri("http://www.bbc.co.uk/iplayer/episode/"+episodeId);

        return location;
    }
    
    private Policy policyFrom(IonOndemandChange ondemand) {
        Policy policy = new Policy();
        policy.setAvailabilityStart(ondemand.getActualStart() == null ? ondemand.getScheduledStart() : ondemand.getActualStart());
        policy.setAvailabilityEnd(ondemand.getEnd() == null ? policy.getAvailabilityStart().plus(ondemand.getDuration()) : ondemand.getEnd());
        return policy;
    }

    private boolean availableNow(Policy policy) {
        if (policy.getAvailabilityStart() == null || policy.getAvailabilityEnd() == null || policy.getAvailabilityStart().isAfter(policy.getAvailabilityEnd())) {
            return false;
        }
        return new Interval(policy.getAvailabilityStart(), policy.getAvailabilityEnd()).contains(clock.now());
    }
}
