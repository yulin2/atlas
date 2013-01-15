package org.atlasapi.query.v2;

import static com.google.common.collect.Iterables.transform;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.persistence.media.channel.ChannelGroupResolver;
import org.atlasapi.persistence.media.channel.ChannelGroupStore;
import org.atlasapi.persistence.media.channel.ChannelResolver;
import org.atlasapi.media.channel.ChannelNumbering;
import org.atlasapi.media.channel.Platform;
import org.atlasapi.media.channel.Region;
import org.atlasapi.media.channel.TemporalString;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.simple.History;
import org.atlasapi.media.entity.simple.PublisherDetails;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.intl.Countries;

public class ChannelSimplifier {
    
    private final NumberToShortStringCodec idCodec;
    private final ChannelResolver channelResolver;
    private final ChannelGroupResolver channelGroupResolver;

    public ChannelSimplifier(NumberToShortStringCodec idCodec, ChannelResolver channelResolver, ChannelGroupResolver channelGroupResolver) {
        this.idCodec = idCodec;
        this.channelResolver = channelResolver;
        this.channelGroupResolver = channelGroupResolver;
    }

    public Iterable<org.atlasapi.media.entity.simple.Channel> simplify(Iterable<Channel> channels, final boolean showChannelGroups, final boolean showHistory, final boolean showParent, final boolean showVariations) {
        return Iterables.transform(channels, new Function<Channel, org.atlasapi.media.entity.simple.Channel>() {

            @Override
            public org.atlasapi.media.entity.simple.Channel apply(Channel input) {
                return simplify(input, showChannelGroups, showHistory, showParent, showVariations);
            }
        });
    }

    public org.atlasapi.media.entity.simple.Channel simplify(Channel input, boolean showChannelGroups, final boolean showHistory, final boolean showParent, final boolean showVariations) {
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
            if (showHistory) {
                simple.setChannels(simplifyToChannelGroups(input.channelNumbers(), showHistory));
            } else {
                simple.setChannels(simplifyToChannelGroups(input.allChannelNumbers(), showHistory));
            }
        }
        if (input.parent() != null) {
            Maybe<Channel> channel = channelResolver.fromId(input.parent());
            if (!channel.hasValue()) {
                throw new RuntimeException("Could not resolve channel with id " +  input.parent());
            }
            if (showParent) {
                simple.setParent(simplify(channel.requireValue(), false, showHistory, false, false));
            } else {
                simple.setParent(toSubChannel(channel.requireValue()));
            }
        }
        if (input.variations() != null && !input.variations().isEmpty()) {
            simple.setVariations(Iterables.transform(
                channelResolver.forIds(input.variations()), 
                new Function<Channel, org.atlasapi.media.entity.simple.Channel>() {
                    @Override
                    public org.atlasapi.media.entity.simple.Channel apply(Channel input) {
                        if (showVariations) {
                            return simplify(input, false, showHistory, false, false);
                        } else {
                            return toSubChannel(input);
                        }
                    }
                }
            ));
        }
        
        if (showHistory) {
            History history = new History();
            history.setStartDate(input.startDate());
            history.setEndDate(input.endDate());
            history.setTitle(simplify(input.allTitles()));
            history.setImage(simplify(input.allImages()));
            
            simple.setHistory(history);
        }

        return simple;
    }
    
    private org.atlasapi.media.entity.simple.Channel toSubChannel(Channel input) {
        org.atlasapi.media.entity.simple.Channel simple = new org.atlasapi.media.entity.simple.Channel();
        simple.setId(idCodec.encode(BigInteger.valueOf(input.getId())));
        simple.setTitle(input.title());
        return simple;
    }

    public List<org.atlasapi.media.entity.simple.ChannelGroup> simplify(List<ChannelGroup> channels, final boolean showChannels, final boolean showHistory) {
        return Lists.transform(channels, new Function<ChannelGroup, org.atlasapi.media.entity.simple.ChannelGroup>() {

            @Override
            public org.atlasapi.media.entity.simple.ChannelGroup apply(ChannelGroup input) {
                return simplify(input, showChannels, showHistory);
            }
        });
    }

    public org.atlasapi.media.entity.simple.ChannelGroup simplify(ChannelGroup input, boolean showChannels, boolean showHistory) {
        org.atlasapi.media.entity.simple.ChannelGroup simple = new org.atlasapi.media.entity.simple.ChannelGroup();
        
        if (input instanceof Platform) {
            simple.setType("platform");
        } else if (input instanceof Region) {
            simple.setType("region");
        }
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
            if (showHistory) {
                simple.setChannels(simplifyToChannels(input.getChannelNumberings(), showHistory));
            } else {
                simple.setChannels(simplifyToChannels(input.getAllChannelNumberings(), showHistory));
            }
        }
        if (input instanceof Platform) {
            simple.setRegions(Iterables.transform(
                channelGroupResolver.channelGroupsFor(((Platform)input).getRegions()), 
                new Function<ChannelGroup, org.atlasapi.media.entity.simple.ChannelGroup>() {
                    @Override
                    public org.atlasapi.media.entity.simple.ChannelGroup apply(ChannelGroup input) {
                        return toSubChannelGroup(input);
                    }
                }
            ));
        }
        if (input instanceof Region) {
            Optional<ChannelGroup> platform = channelGroupResolver.channelGroupFor(((Region)input).getPlatform());
            if (!platform.isPresent()) {
                throw new RuntimeException("Could not resolve platform on Region " +  input.getTitle());
            }
            simple.setPlatform(toSubChannelGroup(platform.get()));
        }
        if (showHistory) {
            History history = new History();
            history.setTitle(simplify(input.getAllTitles()));
            simple.setHistory(history);
        }
        
        return simple;
    }

    private Iterable<org.atlasapi.media.entity.simple.TemporalString> simplify(Iterable<TemporalString> titles) {
        return Iterables.transform(titles, new Function<TemporalString, org.atlasapi.media.entity.simple.TemporalString>() {
            @Override
            public org.atlasapi.media.entity.simple.TemporalString apply(TemporalString input) {
                org.atlasapi.media.entity.simple.TemporalString simple = new org.atlasapi.media.entity.simple.TemporalString();
                simple.setValue(input.getValue());
                simple.setStartDate(input.getStartDate());
                simple.setEndDate(input.getEndDate());
                return simple;
            }
        });
    }

    private Iterable<org.atlasapi.media.entity.simple.ChannelNumbering> simplifyToChannels(Set<ChannelNumbering> channelNumberings, final boolean showHistory) {
        return Iterables.transform(channelNumberings, new Function<ChannelNumbering, org.atlasapi.media.entity.simple.ChannelNumbering>() {
            @Override
            public org.atlasapi.media.entity.simple.ChannelNumbering apply(ChannelNumbering input) {
                org.atlasapi.media.entity.simple.ChannelNumbering simple = new org.atlasapi.media.entity.simple.ChannelNumbering();
                simple.setChannelNumber(input.getChannelNumber());
                Maybe<Channel> channel = channelResolver.fromId(input.getChannel());
                if (!channel.hasValue()) {
                    throw new RuntimeException("Could not resolve channel with id " +  input.getChannel());
                }
                simple.setChannel(simplify(channel.requireValue(), false, showHistory, false, false));
                if (showHistory) {
                    simple.setStartDate(input.getStartDate());
                    simple.setEndDate(input.getEndDate());
                }
                return simple;
            }
        });
    }
    
    private Iterable<org.atlasapi.media.entity.simple.ChannelNumbering> simplifyToChannelGroups(Set<ChannelNumbering> channelNumberings, final boolean showHistory) {
        return Iterables.transform(channelNumberings, new Function<ChannelNumbering, org.atlasapi.media.entity.simple.ChannelNumbering>() {
            @Override
            public org.atlasapi.media.entity.simple.ChannelNumbering apply(ChannelNumbering input) {
                org.atlasapi.media.entity.simple.ChannelNumbering simple = new org.atlasapi.media.entity.simple.ChannelNumbering();
                simple.setChannelNumber(input.getChannelNumber());
                Optional<ChannelGroup> channelGroup = channelGroupResolver.channelGroupFor(input.getChannelGroup());
                if (!channelGroup.isPresent()) {
                    throw new RuntimeException("Could not resolve channelGroup with id " +  input.getChannelGroup());
                }
                simple.setChannelGroup(simplify(channelGroup.get(), false, showHistory));
                if (showHistory) {
                    simple.setStartDate(input.getStartDate());
                    simple.setEndDate(input.getEndDate());
                }
                return simple;
            }
        });
    }

    private org.atlasapi.media.entity.simple.ChannelGroup toSubChannelGroup(ChannelGroup input) {
        org.atlasapi.media.entity.simple.ChannelGroup simple = new org.atlasapi.media.entity.simple.ChannelGroup();
        simple.setId(idCodec.encode(BigInteger.valueOf(input.getId())));
        simple.setTitle(input.getTitle());
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
