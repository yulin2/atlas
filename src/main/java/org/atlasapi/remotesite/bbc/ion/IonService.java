package org.atlasapi.remotesite.bbc.ion;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Policy.Network;
import org.atlasapi.media.entity.Policy.Platform;
import org.atlasapi.remotesite.bbc.BbcLocationPolicyIds;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.intl.Countries;
import com.metabroadcast.common.media.MimeType;

public enum IonService {

    IPLAYER_INTL_STREAM_MP3 {
        @Override
        public void applyTo(Encoding encoding) {
            encoding.setDataContainerFormat(MimeType.AUDIO_MP3);
            encoding.setAudioCoding(MimeType.AUDIO_MP3);
        }

        @Override
        public void applyTo(Policy policy) {
            policy.addAvailableCountry(Countries.ALL);
        }

        @Override
        public List<Policy> policies(Function<MediaSet, Policy> mediaSetToPoliciesFunction) {
            return ImmutableList.copyOf(Iterables.transform(Lists.newArrayList(MediaSet.PC), mediaSetToPoliciesFunction));
        }
    },	
    IPLAYER_INTL_STREAM_AAC_WS_CONCRETE {
        @Override
        public void applyTo(Encoding encoding) {
            encoding.setDataContainerFormat(MimeType.AUDIO_AAC);
            encoding.setAudioCoding(MimeType.AUDIO_AAC);
        }

        @Override
        public void applyTo(Policy policy) {
            policy.addAvailableCountry(Countries.ALL);
        }

        @Override
        public List<Policy> policies(Function<MediaSet, Policy> mediaSetToPoliciesFunction) {
            return ImmutableList.copyOf(Iterables.transform(Lists.newArrayList(MediaSet.PC, MediaSet.APPLE_IPHONE4_HLS, MediaSet.APPLE_PHONE4_IPAD_HLS_3G), mediaSetToPoliciesFunction));
        }
    },	
    IPLAYER_STREAMING_H264_FLV_LO {

        @Override
        public void applyTo(Encoding encoding) {
            encoding.setDataContainerFormat(MimeType.APPLICATION_XSHOCKWAVEFLASH);
            encoding.setVideoCoding(MimeType.VIDEO_H264);
        }

        @Override
        public void applyTo(Policy policy) {
            policy.addAvailableCountry(Countries.GB);
        }

        @Override
        public List<Policy> policies(Function<MediaSet, Policy> mediaSetToPoliciesFunction) {
            return ImmutableList.copyOf(Iterables.transform(Lists.newArrayList(MediaSet.PC), mediaSetToPoliciesFunction));

        }
    },	
    IPLAYER_STREAMING_H264_FLV_VLO {

        @Override
        public void applyTo(Encoding encoding) {
            encoding.setDataContainerFormat(MimeType.APPLICATION_XSHOCKWAVEFLASH);
            encoding.setVideoCoding(MimeType.VIDEO_H264);
        }

        @Override
        public void applyTo(Policy policy) {
            policy.addAvailableCountry(Countries.GB);
        }

        @Override
        public List<Policy> policies(Function<MediaSet, Policy> mediaSetToPoliciesFunction) {          
            return ImmutableList.copyOf(Iterables.transform(Lists.newArrayList(MediaSet.APPLE_IPHONE4_HLS, MediaSet.APPLE_PHONE4_IPAD_HLS_3G), mediaSetToPoliciesFunction));
        }
    },	
    IPLAYER_UK_STREAM_AAC_RTMP_CONCRETE {

        @Override
        public void applyTo(Encoding encoding) {
            encoding.setDataContainerFormat(MimeType.AUDIO_AAC);
            encoding.setAudioCoding(MimeType.AUDIO_AAC);
        }

        @Override
        public void applyTo(Policy policy) {
            policy.addAvailableCountry(Countries.GB);
        }

        @Override
        public List<Policy> policies(Function<MediaSet, Policy> mediaSetToPoliciesFunction) {
            return ImmutableList.copyOf(Iterables.transform(Lists.newArrayList(MediaSet.PC), mediaSetToPoliciesFunction));
        }
    },
    IPLAYER_INTL_STREAM_AAC_RTMP_CONCRETE {

        @Override
        public void applyTo(Encoding encoding) {
            encoding.setDataContainerFormat(MimeType.AUDIO_AAC);
            encoding.setAudioCoding(MimeType.AUDIO_AAC);
        }

        @Override
        public void applyTo(Policy policy) {
            policy.addAvailableCountry(Countries.ALL);
        }

        @Override
        public List<Policy> policies(Function<MediaSet, Policy> mediaSetToPoliciesFunction) {
            return ImmutableList.copyOf(Iterables.transform(Lists.newArrayList(MediaSet.PC), mediaSetToPoliciesFunction));
        }
    },	
    IPLAYER_STREAMING_H264_FLV {
        @Override
        public void applyTo(Encoding encoding) {
            encoding.setDataContainerFormat(MimeType.APPLICATION_XSHOCKWAVEFLASH);
            encoding.setVideoCoding(MimeType.VIDEO_H264);

        }

        @Override
        public void applyTo(Policy policy) {
            policy.addAvailableCountry(Countries.GB);
        }

        @Override
        public List<Policy> policies(Function<MediaSet, Policy> mediaSetToPoliciesFunction) {
            return ImmutableList.copyOf(Iterables.transform(Lists.newArrayList(MediaSet.PC), mediaSetToPoliciesFunction));
        }
    },    
    IPLAYER_STB_UK_STREAM_AAC_CONCRETE {
        @Override
        public void applyTo(Encoding encoding) {
            encoding.setDataContainerFormat(MimeType.AUDIO_AAC);
            encoding.setVideoCoding(MimeType.AUDIO_AAC);
        }

        @Override
        public void applyTo(Policy policy) {
            policy.addAvailableCountry(Countries.GB);
        }

        @Override
        public List<Policy> policies(Function<MediaSet, Policy> mediaSetToPoliciesFunction) {
            return ImmutableList.copyOf(Iterables.transform(Lists.newArrayList(MediaSet.APPLE_IPHONE4_HLS), mediaSetToPoliciesFunction));
        }
    },    
    IPLAYER_UK_STREAM_AAC_RTMP_LO_CONCRETE {
        @Override
        public void applyTo(Encoding encoding) {
            encoding.setDataContainerFormat(MimeType.AUDIO_AAC);
            encoding.setVideoCoding(MimeType.AUDIO_AAC);
        }

        @Override
        public void applyTo(Policy policy) {
            policy.addAvailableCountry(Countries.GB);
        }

        @Override
        public List<Policy> policies(Function<MediaSet, Policy> mediaSetToPoliciesFunction) {
            return ImmutableList.copyOf(Iterables.transform(Lists.newArrayList(MediaSet.APPLE_PHONE4_IPAD_HLS_3G), mediaSetToPoliciesFunction));
        }
    };


    protected abstract void applyTo(Encoding encoding);

    protected abstract void applyTo(Policy policy);

    protected abstract List<Policy> policies(Function<MediaSet, Policy> mediaSetToPoliciesFunction);

    public static class MediaSetsToPoliciesFunction implements Function<MediaSet, Policy> {

        private final BbcLocationPolicyIds locationPolicyIds;

        public MediaSetsToPoliciesFunction(BbcLocationPolicyIds locationPolicyIds) {
            this.locationPolicyIds = checkNotNull(locationPolicyIds);
        }

        @Override
        public Policy apply(MediaSet input) {
            switch (input) {
            case PC :
                Policy pc = new Policy();
                pc.setPlatform(Platform.PC);
                pc.setPlayer(locationPolicyIds.getIPlayerPlayerId());
                pc.setService(locationPolicyIds.getWebServiceId());
                return pc;
            case APPLE_IPHONE4_HLS :
                Policy iosWifi = new Policy();
                iosWifi.setPlatform(Platform.IOS);
                iosWifi.setNetwork(Network.WIFI);
                return iosWifi;
            case APPLE_PHONE4_IPAD_HLS_3G :
                Policy ios3G = new Policy();
                ios3G.setPlatform(Platform.IOS);
                ios3G.setNetwork(Network.THREE_G);
                return ios3G;
            default :
                return null;
            }
        }
    };

    private enum MediaSet {
        PC,
        APPLE_IPHONE4_HLS,
        APPLE_PHONE4_IPAD_HLS_3G;
    }

    public void applyToEncoding(Encoding encoding, Function<MediaSet, Policy> mediaSetToPoliciesFunction) {
        applyTo(encoding);
        List<Policy> policies = policies(mediaSetToPoliciesFunction);
        for (Policy policy : policies) {
            // create matching location for each policy
            Location location = new Location();
            location.setPolicy(policy);
            applyToLocation(location);
            encoding.addAvailableAt(location);
        }
    }

    public List<Location> locations(Function<MediaSet, Policy> mediaSetToPoliciesFunction) {
        List<Policy> policies = policies(mediaSetToPoliciesFunction);
        List<Location> locations = Lists.newArrayList();
        for (Policy policy : policies) {
            Location location = new Location();
            location.setPolicy(policy);
            applyToLocation(location);
            locations.add(location);
        }
        return locations;
    }

    private void applyToLocation(Location location) {
        Policy policy = location.getPolicy();
        applyTo(policy);
    }

    public static Maybe<IonService> fromString(String s) {
        for (IonService service : values()) {
            if (service.name().equalsIgnoreCase(s)) {
                return Maybe.just(service);
            }
        }
        return Maybe.nothing();
    }
}
