package org.atlasapi.query.v2;

import static com.google.common.collect.Iterables.transform;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.channel.ChannelGroup;
import org.atlasapi.media.channel.ChannelGroupResolver;
import org.atlasapi.media.channel.ChannelNumbering;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.media.channel.Platform;
import org.atlasapi.media.channel.Region;
import org.atlasapi.media.channel.TemporalString;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.simple.HistoricalChannelEntry;
import org.atlasapi.media.entity.simple.HistoricalChannelGroupEntry;
import org.atlasapi.media.entity.simple.HistoricalChannelNumberingEntry;
import org.atlasapi.media.entity.simple.PublisherDetails;
import org.joda.time.LocalDate;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.intl.Countries;

public class ChannelSimplifier {
    
    private final NumberToShortStringCodec idCodec;
    private final ChannelResolver channelResolver;
    private final ChannelGroupResolver channelGroupResolver;
    
    private static final Predicate<ChannelNumbering> CURRENT_OR_FUTURE = new Predicate<ChannelNumbering>() {
        @Override
        public boolean apply(ChannelNumbering input) {
            return input.getEndDate() == null || input.getEndDate().isAfter(new LocalDate());
        }
    };

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
        simple.setRegional(input.regional());
        if (input.timeshift() != null) {
            simple.setTimeshift(input.timeshift().getStandardSeconds());
        }
        simple.setAvailableFrom(transform(input.availableFrom(), TO_PUBLISHER_DETAILS));
        simple.setTitle(input.title());
        simple.setImage(input.image());
        simple.setMediaType(input.mediaType() != null ? input.mediaType().toString().toLowerCase() : null);
        simple.setStartDate(input.startDate());            
        simple.setEndDate(input.endDate());            
        
        if(showChannelGroups) {
            simple.setChannelGroups(simplify(input.channelNumbers(), false, showHistory));
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
            simple.setHistory(calculateChannelHistory(input));
        }

        return simple;
    }
    
    private List<HistoricalChannelEntry> calculateChannelHistory(Channel input) {
        List<HistoricalChannelEntry> entries = Lists.newArrayList();
        for (TemporalString title : input.allTitles()) {
            HistoricalChannelEntry entry = new HistoricalChannelEntry();
            entry.setStartDate(title.getStartDate());
            entry.setTitle(title.getValue());
            entry.setImage(input.imageForDate(title.getStartDate()));
            entries.add(entry);
        }
        for (TemporalString image : input.allImages()) {
            HistoricalChannelEntry entry = new HistoricalChannelEntry();
            entry.setStartDate(image.getStartDate());
            entry.setTitle(input.titleForDate(image.getStartDate()));
            entry.setImage(image.getValue());
            entries.add(entry);
        }
        return entries;
    }

    private org.atlasapi.media.entity.simple.Channel toSubChannel(Channel input) {
        org.atlasapi.media.entity.simple.Channel simple = new org.atlasapi.media.entity.simple.Channel();
        simple.setId(idCodec.encode(BigInteger.valueOf(input.getId())));
        simple.setTitle(input.title());
        return simple;
    }

    public List<org.atlasapi.media.entity.simple.ChannelGroup> simplify(List<ChannelGroup> channelGroups, final boolean showChannels, final boolean showHistory) {
        return Lists.transform(channelGroups, new Function<ChannelGroup, org.atlasapi.media.entity.simple.ChannelGroup>() {

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
            simple.setChannels(simplify(input.getChannelNumberings(), true, showHistory));
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
            if (((Region)input).getPlatform() != null) {
                Optional<ChannelGroup> platform = channelGroupResolver.channelGroupFor(((Region)input).getPlatform());
                if (!platform.isPresent()) {
                    throw new RuntimeException("Could not resolve platform on Region " +  input.getTitle());
                }
                simple.setPlatform(toSubChannelGroup(platform.get()));
            }
        }
        if (showHistory) {
            simple.setHistory(calculateChannelHistory(input));
        }
        
        return simple;
    }
    
    private List<HistoricalChannelGroupEntry> calculateChannelHistory(ChannelGroup input) {
        List<HistoricalChannelGroupEntry> entries = Lists.newArrayList();
        for (TemporalString title : input.getAllTitles()) {
            HistoricalChannelGroupEntry entry = new HistoricalChannelGroupEntry();
            entry.setStartDate(title.getStartDate());
            entry.setTitle(title.getValue());
            entries.add(entry);
        }
        return entries;
    }
    
    private Iterable<org.atlasapi.media.entity.simple.ChannelNumbering> simplify(Set<ChannelNumbering> channelNumberings, final boolean toChannels, final boolean showHistory) {
        if (showHistory) {
            final Multimap<Long, ChannelNumbering> channelMapping = ArrayListMultimap.create();
            for (ChannelNumbering numbering : channelNumberings) {
                if (toChannels) {
                    channelMapping.put(numbering.getChannel(), numbering);
                } else {
                    channelMapping.put(numbering.getChannelGroup(), numbering);
                }
            }
            
            return Iterables.concat(Iterables.transform(
                channelMapping.keys(), 
                new Function<Long, Iterable<org.atlasapi.media.entity.simple.ChannelNumbering>>() {
                    @Override
                    public Iterable<org.atlasapi.media.entity.simple.ChannelNumbering> apply(Long input) {
                        Iterable<ChannelNumbering> numberings = channelMapping.get(input);
                        final Iterable<HistoricalChannelNumberingEntry> history = generateHistory(numberings);
                        return simplifyChannelNumberingsWithHistory(numberings, history, toChannels);
                    }
                }
            ));
        } else {
            return Iterables.transform(
                Iterables.filter(channelNumberings, CURRENT_OR_FUTURE), 
                new Function<ChannelNumbering, org.atlasapi.media.entity.simple.ChannelNumbering>() {
                    @Override
                    public org.atlasapi.media.entity.simple.ChannelNumbering apply(ChannelNumbering input) {
                        org.atlasapi.media.entity.simple.ChannelNumbering simple = simplifyNumbering(input, toChannels, showHistory, null);
                        simple.setChannelNumber(input.getChannelNumber());
                        return simple;
                    }
                }
            );
        }
    }

    private Iterable<org.atlasapi.media.entity.simple.ChannelNumbering> simplifyChannelNumberingsWithHistory(
            Iterable<ChannelNumbering> numberings, Iterable<HistoricalChannelNumberingEntry> history, boolean toChannels) {
        
        Iterable<ChannelNumbering> currentNumberings = getCurrentNumberings(numberings);
        
        if (Iterables.isEmpty(currentNumberings)) {
            if (Iterables.isEmpty(numberings)) {
                return ImmutableList.of();
            }
            return ImmutableList.of(simplifyNumbering(Iterables.get(numberings, 0), toChannels, true, history));
        } else {
            List<org.atlasapi.media.entity.simple.ChannelNumbering> simpleNumberings = Lists.newArrayList();
            for (ChannelNumbering currentNumbering : currentNumberings) {
                org.atlasapi.media.entity.simple.ChannelNumbering simple = simplifyNumbering(currentNumbering, toChannels, true, history);
                simple.setChannelNumber(currentNumbering.getChannelNumber());
                simpleNumberings.add(simple);
            }
            return simpleNumberings;
        }
    }
    
    private org.atlasapi.media.entity.simple.ChannelNumbering simplifyNumbering(ChannelNumbering input, boolean toChannels, boolean showHistory, Iterable<HistoricalChannelNumberingEntry> history) {
        org.atlasapi.media.entity.simple.ChannelNumbering simple = new org.atlasapi.media.entity.simple.ChannelNumbering();
        if (toChannels) {
            Maybe<Channel> channel = channelResolver.fromId(input.getChannel());
            Preconditions.checkArgument(channel.hasValue(), "Could not resolve channel with id " +  input.getChannel());
            simple.setChannel(simplify(channel.requireValue(), false, showHistory, false, false));
        } else {
            Optional<ChannelGroup> channelGroup = channelGroupResolver.channelGroupFor(input.getChannelGroup());
            Preconditions.checkArgument(channelGroup.isPresent(), "Could not resolve channelGroup with id " +  input.getChannelGroup());
            simple.setChannelGroup(simplify(channelGroup.get(), false, showHistory));
        }
        if (history != null) {
            simple.setHistory(history);
        }
        return simple;
    }

    private Iterable<HistoricalChannelNumberingEntry> generateHistory(Iterable<ChannelNumbering> numberings) {
        return Iterables.transform(numberings, new Function<ChannelNumbering, HistoricalChannelNumberingEntry>() {
            @Override
            public HistoricalChannelNumberingEntry apply(ChannelNumbering input) {
                HistoricalChannelNumberingEntry entry = new HistoricalChannelNumberingEntry();
                entry.setStartDate(input.getStartDate());
                entry.setChannelNumber(input.getChannelNumber());
                return entry;
            }
        });
    }

    private Iterable<ChannelNumbering> getCurrentNumberings(Iterable<ChannelNumbering> numberings) {
        final LocalDate now = new LocalDate();
        return Iterables.filter(numberings, new Predicate<ChannelNumbering>() {
                @Override
                public boolean apply(ChannelNumbering input) {
                    if (input.getStartDate() != null) {
                        if (input.getEndDate() != null) {
                            return input.getStartDate().compareTo(now) <= 0
                                    && input.getEndDate().compareTo(now) > 0;
                        } else {
                            return input.getStartDate().compareTo(now) <= 0;
                        }
                    } else {
                        return true;
                    }
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
