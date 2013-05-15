package org.atlasapi.remotesite.btvod.model;

import java.util.Map;
import java.util.Set;

import org.atlasapi.media.entity.Policy.Platform;
import org.joda.time.DateTime;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

public class BtVodLocationData {
    private final String uri;
    private final DateTime availabilityStart;
    private final DateTime availabilityEnd;
    private final Integer duration;
    private final Set<Platform> platforms;
    
    public static BtVodLocationDataBuilder builder() {
        return new BtVodLocationDataBuilder();
    }
    
    private BtVodLocationData(String uri, DateTime availabilityStart, DateTime availabilityEnd, int duration, Set<Platform> platforms) {
        this.uri = uri;
        this.availabilityStart = availabilityStart;
        this.availabilityEnd = availabilityEnd;
        this.duration = duration;
        this.platforms = ImmutableSet.copyOf(platforms);
    }
    
    public String getUri() {
        return uri;
    }

    public DateTime getAvailabilityStart() {
        return availabilityStart;
    }

    public DateTime getAvailabilityEnd() {
        return availabilityEnd;
    }

    public Integer getDuration() {
        return duration;
    }

    public Set<Platform> getPlatforms() {
        return platforms;
    }

    public static class BtVodLocationDataBuilder {
        private static final String URI_PREFIX = "http://bt.com/availability_windows/";
        private static final Map<Integer, Platform> platformMapping = ImmutableMap.<Integer, Platform>builder()
                .put(83, Platform.BTVISION_CARDINAL)
                .put(273, Platform.BTVISION_CARDINAL)
                .put(277, Platform.BTVISION_CARDINAL)
                .put(281, Platform.BTVISION_CARDINAL)
                .put(285, Platform.BTVISION_CARDINAL)
                .put(134, Platform.BTVISION_CLASSIC)
                .put(241, Platform.BTVISION_CLASSIC)
                .put(245, Platform.BTVISION_CLASSIC)
                .put(249, Platform.BTVISION_CLASSIC)
                .put(253, Platform.BTVISION_CLASSIC)
                .put(229, Platform.YOUVIEW)
                .put(257, Platform.YOUVIEW)
                .put(261, Platform.YOUVIEW)
                .put(265, Platform.YOUVIEW)
                .put(269, Platform.YOUVIEW)
                .build();
        
        private String uri;
        private DateTime availabilityStart;
        private DateTime availabilityEnd;
        private Integer duration;
        private Set<Platform> platforms;
        
        public BtVodLocationData build() {
            return new BtVodLocationData(uri, availabilityStart, availabilityEnd, duration, platforms);
        }
        
        private BtVodLocationDataBuilder() {
            platforms = Sets.newHashSet();
        }

        public void setUri(String id) {
            this.uri = URI_PREFIX + id;
        }

        public BtVodLocationDataBuilder withUri(String id) {
            setUri(id);
            return this;
        }

        public void setAvailabilityStart(DateTime availabilityStart) {
            this.availabilityStart = availabilityStart;
        }

        public BtVodLocationDataBuilder withAvailabilityStart(DateTime availabilityStart) {
            this.availabilityStart = availabilityStart;
            return this;
        }

        public void setAvailabilityEnd(DateTime availabilityEnd) {
            this.availabilityEnd = availabilityEnd;
        }

        public BtVodLocationDataBuilder withAvailabilityEnd(DateTime availabilityEnd) {
            this.availabilityEnd = availabilityEnd;
            return this;
        }

        public void setDuration(Integer duration) {
            this.duration = duration;
        }

        public BtVodLocationDataBuilder withDuration(Integer duration) {
            this.duration = duration;
            return this;
        }

        public void addPlatform(int platformId) {
            this.platforms.add(platformMapping.get(platformId));
        }

        public BtVodLocationDataBuilder withPlatform(int platformId) {
            addPlatform(platformId);
            return this;
        }
        
    }
}
