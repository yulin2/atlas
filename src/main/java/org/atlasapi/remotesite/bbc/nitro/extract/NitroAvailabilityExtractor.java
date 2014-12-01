package org.atlasapi.remotesite.bbc.nitro.extract;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Set;

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

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.atlas.glycerin.model.Availability;
import com.metabroadcast.atlas.glycerin.model.ScheduledTime;
import com.metabroadcast.common.intl.Countries;
import com.metabroadcast.common.time.DateTimeZones;

/**
 * Possibly extracts an {@link Encoding} and {@link Location}s for it from some
 * {@link Availability}s.
 */
public class NitroAvailabilityExtractor implements ContentExtractor<Iterable<Availability>, Set<Encoding>> {

    private static final String IPLAYER_URL_BASE = "http://www.bbc.co.uk/iplayer/episode/";
    private static final String APPLE_IPHONE4_IPAD_HLS_3G = "apple-iphone4-ipad-hls-3g";
    private static final String APPLE_IPHONE4_HLS = "apple-iphone4-hls";
    private static final String PC = "pc";
    private static final String YOUVIEW = "iptv-all";
    private static final String AVAILABLE = "available";
    private static final int HD_HORIZONTAL_SIZE = 1280;
    private static final int HD_VERTICAL_SIZE = 720;

    private static final int SD_HORIZONTAL_SIZE = 640;
    private static final int SD_VERTICAL_SIZE = 360;

    private static final Predicate<Availability> IS_HD = new Predicate<Availability>() {
        @Override
        public boolean apply(Availability input) {
            return !input.getMediaSet().contains("iptv-sd");
        }
    };

    
    private final Map<String, Platform> mediaSetPlatform = ImmutableMap.of(
        PC, Platform.PC,
        APPLE_IPHONE4_HLS, Platform.IOS,
        APPLE_IPHONE4_IPAD_HLS_3G, Platform.IOS,
        YOUVIEW, Platform.YOUVIEW_IPLAYER
    );

    private final Map<String, Network> mediaSetNetwork = ImmutableMap.of(
        APPLE_IPHONE4_HLS, Network.WIFI,
        APPLE_IPHONE4_IPAD_HLS_3G, Network.THREE_G
    );
    
    @Override
    public Set<Encoding> extract(Iterable<Availability> availabilities) {
        ImmutableSet.Builder<Encoding> encodings = ImmutableSet.builder();

        for (Availability availability : availabilities) {
            Set<Location> locations = getLocationsFor(availability);

            if (!locations.isEmpty()) {
                Encoding encoding = new Encoding();
                encoding.setAvailableAt(locations);
                setHorizontalAndVerticalSize(encoding, IS_HD.apply(availability));
                encodings.add(encoding);
            }
        }

        return encodings.build();
    }

    private void setHorizontalAndVerticalSize(Encoding encoding, boolean isHD) {
        encoding.setVideoHorizontalSize(isHD ? HD_HORIZONTAL_SIZE : SD_HORIZONTAL_SIZE);
        encoding.setVideoVerticalSize(isHD ? HD_VERTICAL_SIZE : SD_VERTICAL_SIZE);
    }

    private Set<Location> getLocationsFor(Availability availability) {
        ImmutableSet.Builder<Location> locations = ImmutableSet.builder();

        for (String mediaSet : availability.getMediaSet()) {
            Platform platform = mediaSetPlatform.get(mediaSet);
            if (platform != null) {
                locations.add(newLocation(availability, platform, mediaSetNetwork.get(mediaSet)));
            }
        }
        return locations.build();
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
        // Even if ActualStart is set, the location may not be available. The Status field 
        // must be referred to in order to confirm that it is.
        //
        // If we've passed the end of the availability window then ingest the actual availability
        // start for reference, since there's the possibility of having missed it if we never
        // ingested during the availability window.
        if (AVAILABLE.equals(source.getStatus())
                || (policy.getAvailabilityEnd() != null && policy.getAvailabilityEnd().isBeforeNow())) {
            policy.setActualAvailabilityStart(toDateTime(source.getActualStart()));
        }
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
