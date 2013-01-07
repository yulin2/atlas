package org.atlasapi.query.v2;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Set;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.inject.internal.Nullable;

public class ChannelFilterer {
    
    public Set<Channel> filter(Iterable<Channel> channels, ChannelFilter filter, @Nullable SetMultimap<Long, ChannelGroup> channelToGroups) {
        
        Preconditions.checkArgument(!(filter.channelGroups.isPresent() && channelToGroups == null));
        
        return ImmutableSet.copyOf(Iterables.filter(channels, filterPredicate(filter, channelToGroups)));
    }
    
    private Predicate<Channel> filterPredicate(final ChannelFilter filter, @Nullable final SetMultimap<Long, ChannelGroup> channelToGroups) {
        return new Predicate<Channel>() {
            @Override
            public boolean apply(Channel input) {
                
                if (filter.broadcaster.isPresent() && !filter.broadcaster.get().equals(input.broadcaster())) {
                    return false;
                }
                
                if (filter.mediaType.isPresent() && !filter.mediaType.get().equals(input.mediaType())) {
                    return false;
                }
                
                if (filter.availableFrom.isPresent() && !input.availableFrom().contains(filter.availableFrom.get())) {
                    return false;
                }
                
                if (filter.channelGroups.isPresent() && Sets.intersection(filter.channelGroups.get(), channelToGroups.get(input.getId())).isEmpty()) {
                    return false;
                }
                
                return true;
            }
        };
    }
    
    public static class ChannelFilter {
        private final Optional<Publisher> broadcaster;
        private final Optional<MediaType> mediaType;
        private final Optional<Publisher> availableFrom;
        private final Optional<Set<ChannelGroup>> channelGroups;
        
        private ChannelFilter(Optional<Publisher> broadcaster, Optional<MediaType> mediaType, Optional<Publisher> availableFrom, Optional<Set<ChannelGroup>> channelGroups) {
            this.broadcaster = broadcaster;
            this.mediaType = mediaType;
            this.availableFrom = availableFrom;
            this.channelGroups = channelGroups;
        }
        
        public boolean channelGroupsFiltered() {
            return channelGroups.isPresent();
        }
        
        public static ChannelFilterBuilder builder() {
            return new ChannelFilterBuilder();
        }
        
        public static class ChannelFilterBuilder {
            private Optional<Publisher> broadcaster = Optional.absent();
            private Optional<MediaType> mediaType = Optional.absent();
            private Optional<Publisher> availableFrom = Optional.absent();
            private Optional<Set<ChannelGroup>> channelGroups = Optional.absent();
            
            private ChannelFilterBuilder() {
            }
            
            public ChannelFilter build() {
                return new ChannelFilter(broadcaster, mediaType, availableFrom, channelGroups);
            }
            
            public ChannelFilterBuilder withBroadcaster(Publisher broadcaster) {
                this.broadcaster = Optional.of(broadcaster);
                return this;
            }
            
            public ChannelFilterBuilder withMediaType(MediaType mediaType) {
                this.mediaType = Optional.of(mediaType);
                return this;
            }
            
            public ChannelFilterBuilder withAvailableFrom(Publisher availableFrom) {
                this.availableFrom = Optional.of(availableFrom);
                return this;
            }
            
            public ChannelFilterBuilder withChannelGroups(Set<ChannelGroup> channelGroups) {
                checkArgument(!channelGroups.isEmpty());
                this.channelGroups = Optional.of(channelGroups);
                return this;
            }
        }
    }
}
