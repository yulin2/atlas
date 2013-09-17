package org.atlasapi.remotesite.bbc.nitro.extract;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Policy.Network;
import org.atlasapi.media.entity.Policy.Platform;
import org.atlasapi.remotesite.ContentExtractor;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.atlas.glycerin.model.Availability;
import com.metabroadcast.atlas.glycerin.model.ScheduledTime;
import com.metabroadcast.common.intl.Countries;
import com.metabroadcast.common.time.DateTimeZones;

/**
 * Extracts {@link Encoding}s and {@link Location}s from {@link Availability}s.
 */
public class NitroAvailabilityExtractor implements ContentExtractor<Availability, Encoding> {

    private static final String IPLAYER_URL_BASE = "http://www.bbc.co.uk/iplayer/episode/";
    private static final String APPLE_IPHONE4_IPAD_HLS_3G = "apple-iphone4-ipad-hls-3g";
    private static final String APPLE_IPHONE4_HLS = "apple-iphone4-hls";
    private static final String PC = "pc";
    
    private final Map<String, Platform> mediaSetPlatform = ImmutableMap.of(
        PC, Platform.PC,
        APPLE_IPHONE4_HLS, Platform.IOS,
        APPLE_IPHONE4_IPAD_HLS_3G, Platform.IOS
    );
    private final Map<String, Network> mediaSetNetwork = ImmutableMap.of(
        APPLE_IPHONE4_HLS, Network.WIFI,
        APPLE_IPHONE4_IPAD_HLS_3G, Network.THREE_G
    );
    
    @Override
    public Encoding extract(Availability source) {
        Encoding encoding = new Encoding();
        ImmutableSet.Builder<Location> locations = ImmutableSet.builder();
        for (String mediaSet : source.getMediaSet()) {
            Platform platform = mediaSetPlatform.get(mediaSet);
            if (platform != null) {
                locations.add(newLocation(source, platform, mediaSetNetwork.get(mediaSet)));
            }
        }
        encoding.setAvailableAt(locations.build());
        return encoding;
    }

    private Location newLocation(Availability source, Platform platform, Network network) {
        Location location = new Location();
        location.setUri(IPLAYER_URL_BASE + checkNotNull(NitroUtil.programmePid(source)));
        location.setTransportType(TransportType.LINK);
        location.setPolicy(policy(source, platform, network));
        return location;
    }

    private Policy policy(Availability source, Platform platform, Network network) {
        Policy policy = new Policy();
        ScheduledTime scheduledTime = source.getScheduledTime();
        if (scheduledTime != null) {
            policy.setAvailabilityStart(toDateTime(scheduledTime.getStart()));
            policy.setAvailabilityEnd(toDateTime(scheduledTime.getEnd()));
        }
        policy.setActualAvailabilityStart(toDateTime(source.getActualStart()));
        policy.setPlatform(platform);
        policy.setNetwork(network);
        policy.setAvailableCountries(ImmutableSet.of(Countries.GB));
        return policy;
    }
    
    private DateTime toDateTime(XMLGregorianCalendar start) {
        if (start == null) {
            return null;
        }
        return new DateTime(start.toGregorianCalendar(), ISOChronology.getInstance())
            .toDateTime(DateTimeZones.UTC);
    }

}
