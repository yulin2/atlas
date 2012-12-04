package org.atlasapi.remotesite.btvod;

import java.util.Set;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;

import org.atlasapi.remotesite.ElementNotFoundException;
import org.atlasapi.remotesite.btvod.model.BtVodItemData;
import org.atlasapi.remotesite.btvod.model.BtVodItemData.BtVodItemDataBuilder;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.common.base.CharMatcher;
import com.google.inject.internal.Sets;

public class BtVodItemDataExtractor {

    public static final String ID_KEY = "id";
    public static final String SELF_KEY = "self";
    public static final String HREF_KEY = "href";
    private static final String LINK_KEY = "link";
    private static final String REL_KEY = "rel";
    private static final String EXTERNAL_ID_KEY = "external-id";
    private static final String METADATA_KEY = "metadata";
    private static final String NAME_KEY = "name";
    private static final String ASSETS_KEY = "assets";
    private static final String ASSET_KEY = "asset";
    private static final String LONG_DESCRIPTION_KEY = "long-description";
    private static final String RELEASE_DATE_KEY = "release-date";
    private static final String SUB_GENRES_KEY = "subgenres";
    private static final String RUNTIME_KEY = "runtime";
    private static final String LANGUAGE_KEY = "language";
    private static final String RATING_KEY = "rating";
    private static final String EPISODE_NUMBER_KEY = "episode-number";
    private static final String SERIES_KEY = "series";
    private static final String TITLE_GROUP_KEY = "title-group";
    private static final String SEASON_NUMBER_KEY = "season-number";
    private static final String AVAILABILITY_WINDOWS_LINK_KEY = "availability_windows";
    private static final String AVAILABILITY_WINDOWS_KEY = "availability-windows";

    private final DateTimeFormatter dateFormatter = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();
    private final DateTimeFormatter timeFormatter = ISODateTimeFormat.hourMinuteSecond();
    private final BtVodLocationDataExtractor locationExtractor = new BtVodLocationDataExtractor();
    
    public BtVodItemData extract(Element source) {
        BtVodItemDataBuilder data = BtVodItemData.builder();

        data.withUri(getChildElement(source, ID_KEY).getValue());
        
        Element metadataLinkElem = getLinkElement(source, METADATA_KEY);
        if (metadataLinkElem == null) {
            throw new ElementNotFoundException(source, LINK_KEY + ":" + METADATA_KEY);
        }        
        extractMetadataFields(data, metadataLinkElem);
        
        Element assetsLinkElem = getLinkElement(source, ASSETS_KEY);
        if (assetsLinkElem == null) {
            throw new ElementNotFoundException(source, LINK_KEY + ":" + ASSETS_KEY);
        } 
        int duration = extractAssetsFields(data, assetsLinkElem);
        
        extractLocations(data, getLinkElement(source, AVAILABILITY_WINDOWS_LINK_KEY), duration);
        
        data.withSelfLink(getLinkElement(source, SELF_KEY).getAttributeValue(HREF_KEY));
        data.withExternalId(getChildElement(source, EXTERNAL_ID_KEY).getValue());
        
        
        Element seriesElement = getLinkElement(source, SERIES_KEY);
        if (seriesElement != null) {
            extractSeriesFields(data, seriesElement);
        }
        extractEpisodeNumber(data, source);
        
        return data.build();
    }

    private void extractLocations(BtVodItemDataBuilder data, Element availabilityLinkElem, int duration) {
        Element availabilityWindowsElem = getChildElement(availabilityLinkElem, AVAILABILITY_WINDOWS_KEY);
        for (int i = 0; i < availabilityWindowsElem.getChildElements().size(); i++) {
            data.addLocation(locationExtractor.extract(availabilityWindowsElem.getChildElements().get(i), duration));
        }
    }

    private void extractSeriesFields(BtVodItemDataBuilder data, Element seriesElement) {
        Element titleGroupElem = getChildElement(seriesElement, TITLE_GROUP_KEY);
        Element metadataElement = getChildElement(getLinkElement(titleGroupElem, METADATA_KEY), METADATA_KEY);
        
        Element seasonNumberElem = getChildElement(metadataElement, SEASON_NUMBER_KEY);
        Element idElement  = getChildElement(titleGroupElem, ID_KEY);
        Element nameElement = getChildElement(titleGroupElem, NAME_KEY);
        Element externalIdElem = getChildElement(titleGroupElem, EXTERNAL_ID_KEY);
        Element selfLink = getLinkElement(titleGroupElem, SELF_KEY);
        
        data.setSeriesNumber(Integer.parseInt(seasonNumberElem.getValue()));
        data.setContainer(idElement.getValue());
        data.setContainerTitle(nameElement.getValue());
        data.setContainerSelfLink(selfLink.getAttributeValue(HREF_KEY));
        data.setContainerExternalId(externalIdElem.getValue());
    }

    private void extractEpisodeNumber(BtVodItemDataBuilder data, Element source) {
        Element episodeNumberElem = source.getFirstChildElement(EPISODE_NUMBER_KEY);
        if (episodeNumberElem != null) {
            data.setEpisodeNumber(Integer.parseInt(episodeNumberElem.getValue()));
        }
    }

    private void extractMetadataFields(BtVodItemDataBuilder data, Element metadataLink) {
        Element metadataElement = getChildElement(metadataLink, METADATA_KEY);
        
        Element nameElement = getChildElement(metadataElement, NAME_KEY);
        Element descriptionElement = getChildElement(metadataElement, LONG_DESCRIPTION_KEY);
        Element dateElement = getChildElement(metadataElement, RELEASE_DATE_KEY);
        Element genresElement = getChildElement(metadataElement, SUB_GENRES_KEY);
        
        data.withTitle(nameElement.getValue())
            .withDescription(descriptionElement.getValue())
            .withYear(dateFormatter.parseDateTime(dateElement.getValue()).getYear());
        
        for (String genre : getGenres(genresElement)) {
            data.addGenre(genre);
        }
    }

    private int extractAssetsFields(BtVodItemDataBuilder data, Element assetsLink) {
        Element assetsElement = getChildElement(getChildElement(assetsLink, ASSETS_KEY), ASSET_KEY);
        Element metadataElement = getChildElement(getLinkElement(assetsElement, METADATA_KEY), METADATA_KEY);
        
        Element languageElement = getChildElement(metadataElement, LANGUAGE_KEY);
        Element ratingElement = getChildElement(metadataElement, RATING_KEY);
        Element runtimeElement = getChildElement(metadataElement, RUNTIME_KEY);
        
        data.withLanguage(languageElement.getValue())
            .withCertificate(ratingElement.getValue());
        
        return extractDurationInSeconds(runtimeElement.getValue());
    }

    private int extractDurationInSeconds(String value) {
        LocalTime time = timeFormatter.parseLocalTime(value);
        return (time.getHourOfDay() * 60 * 60) + (time.getMinuteOfHour() * 60) + time.getSecondOfMinute();
    }

    private Set<String> getGenres(Element genresElement) {
        Set<String> genres = Sets.newHashSet();
        for (int i = 0; i < genresElement.getChildElements().size(); i++) {
            String genre = genresElement.getChildElements().get(i).getValue();
            genre = CharMatcher.WHITESPACE.removeFrom(genre.toLowerCase());
            genre = CharMatcher.anyOf(",.\"'&-").removeFrom(genre);
            genres.add(genre);
        }
        return genres;
    }

    static Element getChildElement(Element source, String childName) {
        Element childElement = source.getFirstChildElement(childName);
        if (childElement == null) {
            throw new ElementNotFoundException(source, childName);
        }
        return childElement;
    }
    
    // gets the element with name = 'link' and attribute 'rel' == rel
    static Element getLinkElement(Element source, String linkType) {
        Elements linkElements = source.getChildElements(LINK_KEY);
        for (int i = 0; i < linkElements.size(); i++) {
            Element linkElem = linkElements.get(i);
            Attribute attr = linkElem.getAttribute(REL_KEY);
            if (attr != null) {
                if (attr.getValue().equals(linkType)) {
                    return linkElem;
                }
            }
        }
        return null;
    }
}
