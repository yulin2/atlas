package org.atlasapi.remotesite.btvod;

import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Version;
import org.atlasapi.media.entity.Policy.Platform;
import org.atlasapi.remotesite.btvod.model.BtVodLocationData;
import org.joda.time.Duration;

import com.metabroadcast.common.intl.Countries;

public class BtVodExtractionHelper {

    private BtVodExtractionHelper() {}

    public static Version generateVersion(BtVodLocationData locationData) {
        Version version = new Version();
        version.setDuration(Duration.standardSeconds(locationData.getDuration()));
        version.setPublishedDuration(locationData.getDuration());
        
        Encoding encoding = new Encoding();
        
        for (Platform platform : locationData.getPlatforms()) {
            Policy policy = new Policy();
            policy.addAvailableCountry(Countries.GB);
            policy.setPlatform(platform);
            policy.setAvailabilityStart(locationData.getAvailabilityStart());
            policy.setAvailabilityEnd(locationData.getAvailabilityEnd());
            
            Location location = new Location();
            location.setUri(locationData.getUri());
            location.setPolicy(policy);
            
            encoding.addAvailableAt(location);
        }
        
        version.addManifestedAs(encoding);
        
        return version;
    }
}
