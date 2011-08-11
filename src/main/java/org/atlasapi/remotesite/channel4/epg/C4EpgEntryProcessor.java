package org.atlasapi.remotesite.channel4.epg;

import static org.atlasapi.media.entity.Publisher.C4;
import static org.atlasapi.persistence.logging.AdapterLogEntry.Severity.WARN;
import static org.atlasapi.remotesite.channel4.C4BroadcastBuilder.broadcast;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Policy.RevenueContract;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.query.content.PerPublisherCurieExpander;
import org.atlasapi.remotesite.channel4.C4AtomApi;
import org.atlasapi.remotesite.channel4.C4EpisodesExtractor;
import org.atlasapi.remotesite.channel4.C4RelatedEntry;
import org.joda.time.DateTime;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.time.DateTimeZones;

public class C4EpgEntryProcessor {

    private static final String TAG_ALIAS_BASE = "tag:www.channel4.com,2009:/programmes/";

    private static final String C4_PROGRAMMES_BASE = "http://www.channel4.com/programmes/";

    private static final Pattern BRAND_ATOM_PATTERN = Pattern.compile("http://\\w{3}.channel4.com/programmes/([a-z0-9\\-]+).atom.*");
    private static final Pattern C40D_PATTERN = Pattern.compile("http://\\w{3}.channel4.com/programmes/([a-z0-9\\-]+)/4od#\\d*");
    private static final Pattern EPISODE_ATOM_PATTERN = Pattern.compile("http://\\w{3}.channel4.com/programmes/([a-z0-9\\-]+)/episode-guide/series-\\d+/episode-\\d+.atom");

    private static final List<Pattern> WEB_SAFE_BRAND_PATTERNS = ImmutableList.of(BRAND_ATOM_PATTERN, C40D_PATTERN, EPISODE_ATOM_PATTERN);
    
    public static final Pattern AVAILABILTY_RANGE_PATTERN = Pattern.compile("start=(.*); end=(.*); scheme=W3C-DTF");

    private final ContentWriter contentWriter;
    private final ContentResolver contentStore;

    private final AdapterLog log;
    
    private final C4SynthesizedItemUpdater c4SynthesizedItemUpdater;
    
    public C4EpgEntryProcessor(ContentWriter contentWriter, ContentResolver contentStore, AdapterLog log) {
        this.contentWriter = contentWriter;
        this.contentStore = contentStore;
        this.log = log;
        this.c4SynthesizedItemUpdater = new C4SynthesizedItemUpdater(contentStore, contentWriter);
    }

    public void process(C4EpgEntry entry, Channel channel) {
        try {

            String webSafeBrandName = webSafeBrandName(entry);

            if (webSafeBrandName == null) {
                throw new IllegalStateException("Couldn't get web-safe brand name for " + entry.id());
            }

            String itemUri = uriFrom(entry, webSafeBrandName);

            Episode episode = null;
            Maybe<Identified> resolved = contentStore.findByCanonicalUris(ImmutableList.of(itemUri)).get(itemUri);
            if (resolved.isNothing()) {
                episode = new Episode(itemUri, PerPublisherCurieExpander.CurieAlgorithm.C4.compact(itemUri), Publisher.C4);
                episode.addAlias(String.format(TAG_ALIAS_BASE+"%s/episode-guide/series-%s/episode-%s", webSafeBrandName, entry.seriesNumber(), entry.episodeNumber()));
            } else {
                episode = (Episode) resolved.requireValue();
            }
            //look for a synthesized equivalent of this item and copy any broadcast/locations and remove its versions.
            updateFromPossibleSynthesized(webSafeBrandName, entry, episode);

            updateEpisodeDetails(episode, entry, channel);
            
            Brand brand = updateBrand(webSafeBrandName, episode, entry);
            contentWriter.createOrUpdate(brand);

            if(episode.getSeriesNumber() != null) {
                updateSeries(C4AtomApi.seriesUriFor(webSafeBrandName, entry.seriesNumber()), webSafeBrandName, episode, brand);
            }

            episode.setContainer(brand);
            contentWriter.createOrUpdate(episode);

        } catch (Exception e) {
            log.record(new AdapterLogEntry(WARN).withCause(e).withSource(getClass()).withDescription("Exception processing entry: " + entry.id()));
        }
    }
    
    private void updateFromPossibleSynthesized(String webSafeBrandName, C4EpgEntry entry, Episode episode) {
        c4SynthesizedItemUpdater.findAndUpdateFromPossibleSynthesized("c4:"+entry.slotId(), episode, C4_PROGRAMMES_BASE+webSafeBrandName);
    }

    private Brand updateBrand(String brandName, Episode episode, C4EpgEntry entry) {
        String brandUri = C4_PROGRAMMES_BASE + brandName;
        Maybe<Identified> resolved = contentStore.findByCanonicalUris(ImmutableSet.of(brandUri)).get(brandUri);
        
        if(resolved.isNothing()) {
            Brand brand = new Brand(brandUri, PerPublisherCurieExpander.CurieAlgorithm.C4.compact(brandUri), C4);
            brand.setTitle(entry.brandTitle());
            brand.addAlias(TAG_ALIAS_BASE+brandName);
            return brand;
        } else {
            return (Brand) resolved.requireValue();
        }
    }

    private void updateSeries(String seriesUri, String brandName, Episode episode, Brand brand) {
        Maybe<Identified> maybeSeries = contentStore.findByCanonicalUris(ImmutableSet.of(seriesUri)).get(seriesUri);
        Series series = null;
        if (maybeSeries.hasValue()) {
            series = (Series) maybeSeries.requireValue();
        } else {
            series = new Series(seriesUri, PerPublisherCurieExpander.CurieAlgorithm.C4.compact(seriesUri), C4);
            series.addAlias(String.format(TAG_ALIAS_BASE+"%s/episode-guide/series-%s", brandName, episode.getSeriesNumber()));
            series.withSeriesNumber(episode.getSeriesNumber());
        }
        series.setParent(brand);
        contentWriter.createOrUpdate(series);
        episode.setSeries(series);
    }

    private Episode updateEpisodeDetails(Episode episode, C4EpgEntry entry, Channel channel) {
        if(episode.getTitle() == null) {
            if (entry.title().equals(entry.brandTitle()) && entry.seriesNumber() != null && entry.episodeNumber() != null) {
                episode.setTitle(String.format(C4EpisodesExtractor.EPISODE_TITLE_TEMPLATE, entry.seriesNumber(), entry.episodeNumber()));
            } else {
                episode.setTitle(entry.title());
            }
        }

        episode.setSeriesNumber(entry.seriesNumber());
        episode.setEpisodeNumber(entry.episodeNumber());

        if(episode.getDescription() == null) {
            episode.setDescription(entry.summary());
        }

        updateVersion(episode, entry, channel);

        if (entry.media() != null && !Strings.isNullOrEmpty(entry.media().thumbnail())) {
            C4AtomApi.addImages(episode, entry.media().thumbnail());
        }

        episode.setIsLongForm(true);
        episode.setMediaType(MediaType.VIDEO);
        episode.setLastUpdated(entry.updated());

        return episode;
    }

    public static void updateVersion(Episode episode, C4EpgEntry entry, Channel channel) {
        Version version = Iterables.getFirst(episode.nativeVersions(), new Version());
        
        if(version.getDuration() == null) {
            version.setDuration(entry.duration());
        }
        
        version.setBroadcasts(updateBroadcasts(version.getBroadcasts(), entry, channel));

        // Don't add/update locations unless this is the first time we've seen the item because
        // we cannot determine the availability start without reading the /4od feed.
        if (version.getManifestedAs().isEmpty()) {
        	Encoding encoding = Iterables.getFirst(version.getManifestedAs(), new Encoding());
        	updateEncoding(version, encoding, entry);
        	if (!encoding.getAvailableAt().isEmpty() && !version.getManifestedAs().contains(encoding)) {
        		version.addManifestedAs(encoding);
        	}
        } else {
        	for (Encoding encoding : version.getManifestedAs()) {
        		for (Location location : encoding.getAvailableAt()) {
        			location.setLastUpdated(entry.updated());
        		}
        	}
        }
        
        if (!episode.getVersions().contains(version)) {
            episode.addVersion(version);
        }
    }

    private static Set<Broadcast> updateBroadcasts(Set<Broadcast> currentBroadcasts, C4EpgEntry entry, Channel channel) {
        Broadcast entryBroadcast = broadcastFrom(entry, channel);
        
        Set<Broadcast> broadcasts = Sets.newHashSet(entryBroadcast);
        for (Broadcast broadcast : currentBroadcasts) {
            if (!entryBroadcast.getId().equals(broadcast.getId())){
                broadcasts.add(broadcast);
            }
        }
        
        return broadcasts;
    }

    private static void updateEncoding(Version version, Encoding encoding, C4EpgEntry entry) {

        if (entry.media() != null && entry.media().player() != null) {
            
            //try to find and update Location with same uri.
            for (Location location : encoding.getAvailableAt()) {
                if(entry.media().player().equals(location.getUri())) {
                    updateLocation(location, entry);
                    return;
                }
            }
            
            //otherwise create a new one.
            Location newLocation = new Location();
            updateLocation(newLocation, entry);
            encoding.addAvailableAt(newLocation);
        }

    }

    static void updateLocation(Location location, C4EpgEntry entry) {
        location.setUri(entry.media().player());
        location.setTransportType(TransportType.LINK);
        location.setPolicy(policyFrom(entry));
		location.setLastUpdated(entry.updated());
    }

    static Policy policyFrom(C4EpgEntry entry) {
        Policy policy = new Policy();
        policy.setLastUpdated(entry.updated());
        policy.setRevenueContract(RevenueContract.FREE_TO_VIEW);
        policy.setAvailableCountries(entry.media().availableCountries());

        Matcher matcher = AVAILABILTY_RANGE_PATTERN.matcher(entry.available());
        if (matcher.matches()) {
        	policy.setAvailabilityStart(new DateTime(matcher.group(1)));
            policy.setAvailabilityEnd(new DateTime(matcher.group(2)));
        }

        return policy;
    }

    private static Broadcast broadcastFrom(C4EpgEntry entry, Channel channel) {
        Broadcast broadcast = broadcast().withChannel(channel.uri()).withTransmissionStart(entry.txDate()).withDuration(entry.duration()).withAtomId(entry.id()).build();

        broadcast.setLastUpdated(entry.updated() != null ? entry.updated() : new DateTime(DateTimeZones.UTC));
        broadcast.setIsActivelyPublished(true);

        return broadcast;
    }

    private String uriFrom(C4EpgEntry entry, String brandName) {
        String canonical = C4AtomApi.canonicaliseEpisodeIdentifier(entry.relatedEntry().getEpisodeIdTag());
        if (canonical == null) {
            throw new IllegalArgumentException("Not a valid c4 episode uri " + canonical);
        }
        return canonical;
    }

    public static String webSafeBrandName(C4EpgEntry entry) {
        C4RelatedEntry related = entry.relatedEntry();
        if (related != null) {
            return C4AtomApi.webSafeNameFromAnyFeedId(related.getEpisodeIdTag());
        }
        for (String link : entry.links()) {
            for (Pattern pattern : WEB_SAFE_BRAND_PATTERNS) {
                Matcher matcher = pattern.matcher(link);
                if (matcher.matches()) {
                    return matcher.group(1);
                }
            }
        }
        return null;
    }
}
