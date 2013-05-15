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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Sets;

public class BtVodItemDataExtractor {

    public static final String ID_KEY = "id";
    public static final String SELF_KEY = "self";
    public static final String HREF_KEY = "href";
    public static final String LINK_KEY = "link";
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
    private final Logger log = LoggerFactory.getLogger(BtVodItemDataExtractor.class);
    
    public BtVodItemData extract(Element source) {
        BtVodItemDataBuilder data = BtVodItemData.builder();

        extractId(data, source);
        
        extractMetadataFields(data, source);
        
        Integer duration = extractAssetsFields(data, source);
        
        extractLocations(data, source, duration);
        extractSelfLink(data, source);
        extractExternalId(data, source);
        extractSeriesFields(data, source);
        extractEpisodeNumber(data, source);
        
        return data.build();
    }
    
    private void extractExternalId(BtVodItemDataBuilder data, Element source) {
        Element idElem = source.getFirstChildElement(EXTERNAL_ID_KEY);
        if (idElem == null) {
            throw new ElementNotFoundException(source, EXTERNAL_ID_KEY);
        }
        data.setExternalId(idElem.getValue());
    }

    private void extractSelfLink(BtVodItemDataBuilder data, Element source) {
        Element selfLinkElem = getLinkElement(source, SELF_KEY);
        if (selfLinkElem != null) {
            data.setSelfLink(selfLinkElem.getAttributeValue(HREF_KEY));
        } else {
            log.debug("No child element with name " + SELF_KEY + " found on element " + source);
        }
    }

    private void extractId(BtVodItemDataBuilder data, Element source) {
        Element idElem = source.getFirstChildElement(ID_KEY);
        if (idElem == null) {
            throw new ElementNotFoundException(source, ID_KEY);
        }
        data.setUri(idElem.getValue());
    }

    private void extractLocations(BtVodItemDataBuilder data, Element source, Integer duration) {
        Element availabilityLinkElem = getLinkElement(source, AVAILABILITY_WINDOWS_LINK_KEY);
        if (availabilityLinkElem == null) {
            throw new ElementNotFoundException(source, LINK_KEY + ":" + AVAILABILITY_WINDOWS_LINK_KEY);
        }
        Element availabilityWindowsElem = availabilityLinkElem.getFirstChildElement(AVAILABILITY_WINDOWS_KEY);
        if (availabilityWindowsElem == null) {
            throw new ElementNotFoundException(availabilityLinkElem, AVAILABILITY_WINDOWS_KEY);
        }
        
        for (int i = 0; i < availabilityWindowsElem.getChildElements().size(); i++) {
            data.addLocation(locationExtractor.extract(availabilityWindowsElem.getChildElements().get(i), duration));
        }
    }

    private void extractSeriesFields(BtVodItemDataBuilder data, Element source) {
        Element seriesElement = getLinkElement(source, SERIES_KEY);
        if (seriesElement == null) {
            return;
        }
        Element titleGroupElem = seriesElement.getFirstChildElement(TITLE_GROUP_KEY);
        if (titleGroupElem == null) {
            return;
        }
        
        extractSeasonNumber(data, titleGroupElem);
        extractContainerSelfLink(data, titleGroupElem);
        extractContainer(data, titleGroupElem);
        extractContainerTitle(data, titleGroupElem);
        extractContainerExternalId(data, titleGroupElem);
    }
    
    private void extractContainerSelfLink(BtVodItemDataBuilder data, Element titleGroupElem) {
        Element selfLinkElem = getLinkElement(titleGroupElem, SELF_KEY);
        if (selfLinkElem != null) {
            data.setContainerSelfLink(selfLinkElem.getAttributeValue(HREF_KEY));
        }
    }
    
    private void extractContainer(BtVodItemDataBuilder data, Element titleGroupElem) {
        Element containerElem = titleGroupElem.getFirstChildElement(ID_KEY);
        if (containerElem == null) {
            throw new ElementNotFoundException(titleGroupElem, ID_KEY);
        }
        data.setContainer(containerElem.getValue());
    }
    
    private void extractContainerTitle(BtVodItemDataBuilder data, Element titleGroupElem) {
        Element containerTitleElem = titleGroupElem.getFirstChildElement(NAME_KEY);
        if (containerTitleElem == null) {
            throw new ElementNotFoundException(titleGroupElem, NAME_KEY);
        }
        data.setContainerTitle(containerTitleElem.getValue());
    }
    
    private void extractContainerExternalId(BtVodItemDataBuilder data, Element titleGroupElem) {
        Element containerExternalIdElem = titleGroupElem.getFirstChildElement(EXTERNAL_ID_KEY);
        if (containerExternalIdElem == null) {
            throw new ElementNotFoundException(titleGroupElem, EXTERNAL_ID_KEY);
        }
        data.setContainerExternalId(containerExternalIdElem.getValue());
    }
    
    private void extractSeasonNumber(BtVodItemDataBuilder data, Element titleGroupElem) {
        Element metadataLinkElem = getLinkElement(titleGroupElem, METADATA_KEY);
        if (metadataLinkElem == null) {
            return;
        }
        Element metadataElement = metadataLinkElem.getFirstChildElement(METADATA_KEY);
        if (metadataElement == null) {
            return;
        }

        Element seasonNumberElem = metadataElement.getFirstChildElement(SEASON_NUMBER_KEY);
        if (seasonNumberElem != null) {
            data.setSeriesNumber(Integer.parseInt(seasonNumberElem.getValue()));
        }
    }

    private void extractEpisodeNumber(BtVodItemDataBuilder data, Element source) {
        Element episodeNumberElem = source.getFirstChildElement(EPISODE_NUMBER_KEY);
        if (episodeNumberElem != null) {
            data.setEpisodeNumber(Integer.parseInt(episodeNumberElem.getValue()));
        }
    }

    private void extractMetadataFields(BtVodItemDataBuilder data, Element source) {
        Element metadataLinkElem = getLinkElement(source, METADATA_KEY);
        if (metadataLinkElem == null) {
            throw new ElementNotFoundException(source, LINK_KEY + ":" + METADATA_KEY);
        }  
        Element metadataElement = metadataLinkElem.getFirstChildElement(METADATA_KEY);
        if (metadataElement == null) {
            throw new ElementNotFoundException(metadataLinkElem, METADATA_KEY);
        }
        
        extractTitle(data, metadataElement);
        extractDescription(data, metadataElement);
        extractYear(data, metadataElement);
        extractGenres(data, metadataElement);
    }

    private void extractTitle(BtVodItemDataBuilder data, Element metadataElement) {
        Element nameElement = metadataElement.getFirstChildElement(NAME_KEY);
        if (nameElement == null) {
            throw new ElementNotFoundException(metadataElement, NAME_KEY);
        }
        data.setTitle(nameElement.getValue());
    }

    private void extractDescription(BtVodItemDataBuilder data, Element metadataElement) {
        Element descriptionElement = metadataElement.getFirstChildElement(LONG_DESCRIPTION_KEY);
        if (descriptionElement == null) {
            throw new ElementNotFoundException(metadataElement, LONG_DESCRIPTION_KEY);
        }
        data.setDescription(descriptionElement.getValue());
    }

    private void extractYear(BtVodItemDataBuilder data, Element metadataElement) {
        Element dateElement = metadataElement.getFirstChildElement(RELEASE_DATE_KEY);
        if (dateElement != null) {
            data.setYear(dateFormatter.parseDateTime(dateElement.getValue()).getYear());
        }
    }

    private void extractGenres(BtVodItemDataBuilder data, Element metadataElement) {
        Element genresElement = metadataElement.getFirstChildElement(SUB_GENRES_KEY);
        if (genresElement != null) {
            for (String genre : getGenres(genresElement)) {
                data.addGenre(genre);
            }
        }
    }

    private Integer extractAssetsFields(BtVodItemDataBuilder data, Element source) {
        Element assetsLinkElem = getLinkElement(source, ASSETS_KEY);
        if (assetsLinkElem == null) {
            return null;
        }
        Element assetsElement = assetsLinkElem.getFirstChildElement(ASSETS_KEY);
        if (assetsElement == null) {
            return null;
        }
        Element assetElement = assetsElement.getFirstChildElement(ASSET_KEY);
        if (assetElement == null) {
            return null;
        }
        Element metadataLinkElem = getLinkElement(assetElement, METADATA_KEY);
        if (metadataLinkElem == null) {
            return null;
        }  
        Element metadataElement = metadataLinkElem.getFirstChildElement(METADATA_KEY);
        if (metadataElement == null) {
            return null;
        }
        
        extractLanguage(data, metadataElement);
        extractRating(data, metadataElement);
        
        Element runtimeElement = metadataElement.getFirstChildElement(RUNTIME_KEY);
        if (runtimeElement == null) {
            return null;
        }
        return extractDurationInSeconds(runtimeElement.getValue());
    }

    private void extractLanguage(BtVodItemDataBuilder data, Element metadataElement) {
        Element languageElement = metadataElement.getFirstChildElement(LANGUAGE_KEY);
        if (languageElement != null) {
            data.setLanguage(languageElement.getValue());
        }
    }

    private void extractRating(BtVodItemDataBuilder data, Element metadataElement) {
        Element ratingElement = metadataElement.getFirstChildElement(RATING_KEY);
        if (ratingElement != null) {
            data.setCertificate(ratingElement.getValue());
        }
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
