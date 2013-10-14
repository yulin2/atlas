package org.atlasapi.remotesite.talktalk;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.XMLGregorianCalendar;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Certificate;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Policy.Platform;
import org.atlasapi.media.entity.Policy.RevenueContract;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.talktalk.vod.bindings.AvailabilityType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ChannelType;
import org.atlasapi.remotesite.talktalk.vod.bindings.GenreListType;
import org.atlasapi.remotesite.talktalk.vod.bindings.GenreType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemTypeType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ProductTypeType;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.intl.Countries;
import com.metabroadcast.common.time.DateTimeZones;

/**
 * Extracts an {@link Item} from TalkTalk {@link ItemDetailType} according to <a
 * href="http://docs.metabroadcast.com/display/mbst/TalkTalk+VOD">http://docs.
 * metabroadcast.com/display/mbst/TalkTalk+VOD</a>
 * 
 */
public class TalkTalkItemDetailItemExtractor {

    private static final Pattern NUMBERED_EPISODE_TITLE = Pattern.compile("^Ep\\s*(\\d+)\\s*:\\s*(.*)");
    private static final String MOVIE_CHANNEL_GENRE_CODE = "CHMOVIES";
    
    private static final Map<ProductTypeType, RevenueContract> revenueContractLookup =  ImmutableMap.of(
            ProductTypeType.FREE, RevenueContract.FREE_TO_VIEW, 
            ProductTypeType.RENTAL, RevenueContract.PAY_TO_RENT, 
            ProductTypeType.SUBSCRIPTION, RevenueContract.SUBSCRIPTION);
    
    
    private static final PeriodFormatter durationFormatter = new PeriodFormatterBuilder()
        .appendLiteral("0 ")
        .appendHours()
        .appendSeparator(":")
        .appendMinutes()
        .appendSeparator(":")
        .appendSeconds()
        .toFormatter();
    
    private static final TalkTalkDescriptionExtractor descriptionExtractor = new TalkTalkDescriptionExtractor();
    private static final TalkTalkGenresExtractor genresExtractor = new TalkTalkGenresExtractor();
    private static final TalkTalkImagesExtractor imagesExtractor = new TalkTalkImagesExtractor();
    private static final TalkTalkUriCompiler uriCompiler = new TalkTalkUriCompiler();

    public Item extract(ItemDetailType detail, Optional<Brand> brand, Optional<Series> series) {
        checkArgument(ItemTypeType.EPISODE.equals(detail.getItemType()), 
                "Can't extract Item from non-EPISODE Item type");
        return (brand.isPresent() || series.isPresent()) ? extractEpisode(detail, brand, series)
                                                         : extractItem(detail);
    }

    private Episode extractEpisode(ItemDetailType detail, Optional<Brand> brand,
            Optional<Series> series) {
        Episode episode = setCommonItemFields(new Episode(), detail);
        episode.setEpisodeNumber(getEpisodeNumber(detail.getTitle()));
        if (brand.isPresent()) {
            episode.setContainer(brand.get());
        }
        if (series.isPresent()) {
            episode.setSeriesNumber(series.get().getSeriesNumber());
            episode.setSeries(series.get());
            if (!brand.isPresent()) {
                episode.setContainer(series.get());
            }
        }
        return episode;
    }

    private Integer getEpisodeNumber(String title) {
        if (title == null) {
            return null;
        }
        Matcher matcher = NUMBERED_EPISODE_TITLE.matcher(title);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    private Item extractItem(ItemDetailType detail) {
        Item item;
        if (isFilm(detail)) {
            item = new Film();
        } else {
            item = new Item();
        }
        return setCommonItemFields(item, detail);
    }

    private <I extends Item> I setCommonItemFields(I item, ItemDetailType detail) {
        item.setCanonicalUri(uriCompiler.uriFor(detail));
        item.setPublisher(Publisher.TALK_TALK);
        item.setTitle(removeNumberPrefix(detail.getTitle()));
        item = descriptionExtractor.extractDescriptions(item, detail.getSynopsisList());
        item.setCertificates(extractCertificates(detail));
        item.setGenres(genresExtractor.extract(detail));
        item.setImages(imagesExtractor.extract(detail));
        item.setVersions(extractVersions(detail));
        return item;
    }

    private String removeNumberPrefix(String title) {
        if (Strings.isNullOrEmpty(title)) {
            return title;
        }
        Matcher matcher = NUMBERED_EPISODE_TITLE.matcher(title);
        if (matcher.matches()) {
            return matcher.group(2).trim();
        }
        return title;
    }

    private Set<Version> extractVersions(ItemDetailType detail) {
        Version version = new Version();
        if (detail.getDuration() != null) {
            version.setDuration(extractDuration(detail.getDuration()));
            version.setPublishedDuration(version.getDuration());
        }
        version.setManifestedAs(extractEncodings(detail));
        return ImmutableSet.of(version);
    }

    private Duration extractDuration(String duration) {
        return durationFormatter.parsePeriod(duration).toStandardDuration();
    }

    private Set<Encoding> extractEncodings(ItemDetailType detail) {
        Encoding encoding = new Encoding();
        encoding.setAvailableAt(extractLocations(detail));
        return ImmutableSet.of(encoding);
    }

    private Set<Location> extractLocations(ItemDetailType detail) {
        Location location = new Location(); 
        location.setPolicy(extractPolicy(detail));
        return ImmutableSet.of(location);
    }

    private Policy extractPolicy(ItemDetailType detail) {
        Policy policy = new Policy();
        AvailabilityType availability = detail.getAvailability();
        if (availability != null) {
            policy.setAvailabilityStart(toDateTime(availability.getStartDt()));
            policy.setAvailabilityEnd(toDateTime(availability.getEndDt()));
        }
        policy.setPlatform(Platform.TALK_TALK);
        policy.setAvailableCountries(ImmutableSet.of(Countries.GB));
        policy.setRevenueContract(revenueContractLookup.get(detail.getProductType()));
        return policy;
    }

    private DateTime toDateTime(XMLGregorianCalendar xmlGc) {
        if (xmlGc == null) {
            return null;
        }
        return new DateTime(xmlGc.toGregorianCalendar(), ISOChronology.getInstance())
            .toDateTime(DateTimeZones.UTC);
    }

    private Iterable<Certificate> extractCertificates(ItemDetailType detail) {
        String ratingCode = detail.getRatingCode();
        if (ratingCode != null) {
            return ImmutableSet.of(new Certificate(ratingCode, Countries.GB));
        }
        return ImmutableSet.of();
    }

    private boolean isFilm(ItemDetailType detail) {
        ChannelType channel = detail.getChannel();
        if (channel != null) {
            GenreListType channelGenreList = channel.getChannelGenreList();
            if (channelGenreList != null) {
                for (GenreType genre : channelGenreList.getGenre()) {
                    if (MOVIE_CHANNEL_GENRE_CODE.equals(genre.getGenreCode())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
