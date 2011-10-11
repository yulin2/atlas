package org.atlasapi.remotesite.redux;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.Set;

import org.atlasapi.media.TransportSubType;
import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Policy.RevenueContract;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.redux.model.FullReduxProgramme;
import org.atlasapi.remotesite.redux.model.ReduxMedia;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.metabroadcast.common.intl.Countries;
import com.metabroadcast.common.media.MimeType;

public class FullProgrammeItemExtractor implements ContentExtractor<FullReduxProgramme, Item>{

    private static final DateTimeFormatter ISO_FORMAT = ISODateTimeFormat.dateTimeNoMillis();
    private static final String IMAGE_SUFFIX = "image-640.jpg";
    private static final String THUMBNAIL_SUFFIX = "image-74.jpg";
    public static final String CANONICAL_URI_BASE = "http://g.bbcredux.com";
    private static final String CURIE_BASE = "redux:";
    private static final String LOCATION_URI_FORMAT = "http://devapi.bbcredux.com/programme/%s/media/%s";
    
    private final AdapterLog log;
    
    public FullProgrammeItemExtractor(AdapterLog log) {
        this.log = log;
    }
    
    @Override
    public Item extract(FullReduxProgramme source) {
        checkNotNull(source, "Attempt to extract Item from null ReduxProgramme");
        checkNotNull(Strings.emptyToNull(source.getCanonical()), "Non-empty 'canonical' field required");
        checkNotNull(Strings.emptyToNull(source.getDiskref()), "Non empty 'diskref' field required");
        
        Item item = new Item(CANONICAL_URI_BASE + source.getCanonical(), CURIE_BASE + source.getDiskref(), Publisher.BBC_REDUX);
        if(!Strings.isNullOrEmpty(source.getUri())) {
            item.addAlias(CANONICAL_URI_BASE + source.getUri());
        }
        
        item.setTitle(source.getTitle());
        item.setDescription(source.getDescription());
        
        item.setImage(source.getDepiction());
        item.setThumbnail(thumbnailFrom(source.getDepiction()));

        item.setMediaType(ReduxServices.mediaTypeForService(source.getService()));
        item.setSpecialization(ReduxServices.specializationForService(source.getService()));
        
        item.setVersions(ImmutableSet.of(
            versionFrom(source)
        ));
        
        return item;
    }

    private Version versionFrom(FullReduxProgramme source) {
        Version version = new Version();
        
        Broadcast broadcast = broadcastFrom(source);
        
        if (broadcast != null) {
            version.setBroadcasts(ImmutableSet.of(
                    broadcast
            ));
            version.setPublishedDuration(broadcast.getBroadcastDuration());
            version.setManifestedAs(encodingsFrom(source, broadcast));
        }
        
        return version;
    }

    private Set<Encoding> encodingsFrom(FullReduxProgramme source, Broadcast broadcast) {
        Builder<Encoding> encodings = ImmutableSet.<Encoding>builder();
        
        for (ReduxMedia media : source.getMedia().values()) {
            Encoding encoding = encodingFrom(media, source.getDiskref(), source.getKey(), broadcast.getTransmissionEndTime());
            if (encoding != null) {
                encodings.add(encoding);
            }
        }
        
        return encodings.build();
    }

    private Encoding encodingFrom(ReduxMedia media, String diskref, String key, DateTime availableFrom) {
        try {
            Encoding encoding = new Encoding();
            
            encoding.setVideoCoding(MimeType.fromString(media.getType()));
            
            encoding.setAvailableAt(ImmutableSet.of(
                locationFrom(media, diskref, key, availableFrom)
            ));
            
            return encoding;
        } catch (Exception e) {
            log.record(warnEntry().withCause(e).withSource(getClass()).withDescription("Exception extracting encoding %s for diskref %s", media.getType(), diskref));
            return null;
        }
    }

    private Location locationFrom(ReduxMedia media, String diskref, String key, DateTime availableFrom) {
        Location location = new Location();

        Policy policy = new Policy();
        policy.setAvailabilityStart(availableFrom);
        policy.setAvailableCountries(ImmutableSet.of(Countries.ALL));
        policy.setRevenueContract(RevenueContract.PRIVATE);
        location.setPolicy(policy);

        location.setTransportType(TransportType.DOWNLOAD);
        location.setTransportSubType(TransportSubType.HTTP);
        location.setAvailable(true);
        
        location.setUri(String.format(LOCATION_URI_FORMAT, diskref, key));
        
        return location;
    }

    private Broadcast broadcastFrom(FullReduxProgramme source) {
        Channel channel = ReduxServices.CHANNEL_MAP.get(source.getService());
        Duration duration = getDuration(source);
        if (channel != null && duration != null) {
            Broadcast broadcast = new Broadcast(channel.uri(), ISO_FORMAT.parseDateTime(source.getWhen()), duration);
            
            broadcast.setSigned(source.getSigned());
            broadcast.setSubtitled(source.getSubtitles());
            broadcast.setHighDefinition(source.getHd());
            broadcast.setAudioDescribed(source.getAd());
            
            return broadcast;
        }
        log.record(warnEntry().withSource(getClass()).withDescription("Couldn't extract broadcast for diskref %s", source.getDiskref()));
        return null;
    }

    private Duration getDuration(FullReduxProgramme source) {
        try {
            return Duration.standardSeconds(Long.parseLong(source.getDuration()));
        } catch (Exception e) {
            log.record(warnEntry().withCause(e).withDescription("Diskref %s, couldn't parse duration %s to long", source.getDiskref(), source.getDuration()).withSource(getClass()));
            return null;
        }
    }

    private String thumbnailFrom(String depiction) {
        if (Strings.isNullOrEmpty(depiction)) {
            return null;
        }
        
        int suffixStart = depiction.indexOf(IMAGE_SUFFIX);
        if (suffixStart > 0 && suffixStart + IMAGE_SUFFIX.length() == depiction.length()) {
            return depiction.substring(suffixStart) + THUMBNAIL_SUFFIX;
        }
        return null;
    }

}
