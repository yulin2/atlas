package org.atlasapi.query.v2;

import static com.google.common.collect.Iterables.transform;

import java.math.BigInteger;
import java.util.List;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupStore;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.simple.PublisherDetails;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.intl.Countries;

public class ChannelSimplifier {
    
    private final NumberToShortStringCodec idCodec;
    private final ChannelResolver channelResolver;
    private final ChannelGroupStore channelGroupResolver;

    public ChannelSimplifier(NumberToShortStringCodec idCodec, ChannelResolver channelResolver, ChannelGroupStore channelGroupResolver) {
        this.idCodec = idCodec;
        this.channelResolver = channelResolver;
        this.channelGroupResolver = channelGroupResolver;
    }

    public Iterable<org.atlasapi.media.entity.simple.Channel> simplify(Iterable<Channel> channels, final boolean showChannelGroups) {
        return Iterables.transform(channels, new Function<Channel, org.atlasapi.media.entity.simple.Channel>() {

            @Override
            public org.atlasapi.media.entity.simple.Channel apply(Channel input) {
                return simplify(input, showChannelGroups);
            }
        });
    }

    public org.atlasapi.media.entity.simple.Channel simplify(Channel input, boolean showChannelGroups) {
        org.atlasapi.media.entity.simple.Channel simple = new org.atlasapi.media.entity.simple.Channel();

        simple.setType("channel");
        simple.setUri(input.getCanonicalUri());
        if (input.getId() != null) {
            simple.setId(idCodec.encode(BigInteger.valueOf(input.getId())));
        }
        simple.setAliases(input.getAliases());
        simple.setPublisherDetails(toPublisherDetails(input.source()));
        simple.setBroadcaster(toPublisherDetails(input.broadcaster()));
        simple.setHighDefinition(input.highDefinition());
        simple.setAvailableFrom(transform(input.availableFrom(), TO_PUBLISHER_DETAILS));
        simple.setTitle(input.title());
        simple.setImage(input.image());
        simple.setMediaType(input.mediaType() != null ? input.mediaType().toString().toLowerCase() : null);
        
        if(showChannelGroups) {
            simple.setChannelGroups(simplify(ImmutableList.copyOf(channelGroupResolver.channelGroupsFor(input)),false));
        }

        return simple;
    }
    
    public List<org.atlasapi.media.entity.simple.ChannelGroup> simplify(List<ChannelGroup> channels, final boolean showChannels) {
        return Lists.transform(channels, new Function<ChannelGroup, org.atlasapi.media.entity.simple.ChannelGroup>() {

            @Override
            public org.atlasapi.media.entity.simple.ChannelGroup apply(ChannelGroup input) {
                return simplify(input, showChannels);
            }
        });
    }

    public org.atlasapi.media.entity.simple.ChannelGroup simplify(ChannelGroup input, boolean showChannels) {
        org.atlasapi.media.entity.simple.ChannelGroup simple = new org.atlasapi.media.entity.simple.ChannelGroup();
        
        simple.setType(input.getType().key());
        simple.setUri(input.getCanonicalUri());
        if (input.getId() != null) {
            simple.setId(idCodec.encode(BigInteger.valueOf(input.getId())));
        }
        simple.setAliases(input.getAliases());
        simple.setPublisherDetails(toPublisherDetails(input.getPublisher()));
        simple.setTitle(input.getTitle());
        if (input.getAvailableCountries() != null) {
            simple.setAvailableCountries(Countries.toCodes(input.getAvailableCountries()));
        }
        if(showChannels) {
            simple.setChannels(ImmutableSet.copyOf(simplify(channelResolver.forIds(input.getChannels()),false)));
        }
        
        return simple;
    }
    
    private static final Function<Publisher, PublisherDetails> TO_PUBLISHER_DETAILS = new Function<Publisher, PublisherDetails>() {
        @Override
        public PublisherDetails apply(Publisher input) {
            return toPublisherDetails(input);
        }
    };

    private static PublisherDetails toPublisherDetails(Publisher publisher) {

        if (publisher == null) {
            return null;
        }

        PublisherDetails details = new PublisherDetails(publisher.key());

        if (publisher.country() != null) {
            details.setCountry(publisher.country().code());
        }

        details.setName(publisher.title());
        return details;
    }
}
