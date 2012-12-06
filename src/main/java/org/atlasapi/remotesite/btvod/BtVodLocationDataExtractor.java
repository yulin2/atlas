package org.atlasapi.remotesite.btvod;

import static org.atlasapi.remotesite.btvod.BtVodItemDataExtractor.ID_KEY;
import static org.atlasapi.remotesite.btvod.BtVodItemDataExtractor.LINK_KEY;
import static org.atlasapi.remotesite.btvod.BtVodItemDataExtractor.getLinkElement;
import nu.xom.Element;

import org.atlasapi.remotesite.ElementNotFoundException;
import org.atlasapi.remotesite.btvod.model.BtVodLocationData;
import org.atlasapi.remotesite.btvod.model.BtVodLocationData.BtVodLocationDataBuilder;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BtVodLocationDataExtractor {
    
    private static final String FROM_KEY = "from";
    private static final String TO_KEY = "to";
    private static final String PLATFORMS_KEY = "platforms";
    
    private final DateTimeFormatter dateFormatter = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();
    private final Logger log = LoggerFactory.getLogger(BtVodLocationDataExtractor.class);

    public BtVodLocationData extract(Element source, Integer duration) {
        BtVodLocationDataBuilder locationData = BtVodLocationData.builder();
        
        extractId(locationData, source);
        extractAvailabilityStart(locationData, source);
        extractAvailabilityEnd(locationData, source);
        
        locationData.setDuration(duration);
        
        extractPlatforms(locationData, source);
        
        return locationData.build();
    }
    
    private void extractAvailabilityEnd(BtVodLocationDataBuilder locationData, Element source) {
        Element availabilityStartElem = source.getFirstChildElement(FROM_KEY);
        if (availabilityStartElem != null) {
            locationData.setAvailabilityStart(dateFormatter.parseDateTime(availabilityStartElem.getValue()));
        } else {
            log.debug("No child element with name " + FROM_KEY + " found on element " + source);
        }
    }

    private void extractAvailabilityStart(BtVodLocationDataBuilder locationData, Element source) {
        Element availabilityEndElem = source.getFirstChildElement(TO_KEY);
        if (availabilityEndElem != null) {
            locationData.setAvailabilityEnd(dateFormatter.parseDateTime(availabilityEndElem.getValue()));
        } else {
            log.debug("No child element with name " + TO_KEY + " found on element " + source);
        }
    }

    private void extractId(BtVodLocationDataBuilder locationData, Element source) {
        Element idElem = source.getFirstChildElement(ID_KEY);
        if (idElem == null) {
            throw new ElementNotFoundException(source, ID_KEY);
        }
        locationData.setUri(idElem.getValue());
    }

    private void extractPlatforms(BtVodLocationDataBuilder locationData, Element source) {
        Element platformsLinkElem = getLinkElement(source, PLATFORMS_KEY);
        if (platformsLinkElem == null) {
            throw new ElementNotFoundException(source, LINK_KEY + ":" + PLATFORMS_KEY);
        }
        Element platformsElem = platformsLinkElem.getFirstChildElement(PLATFORMS_KEY);
        if (platformsElem == null) {
            throw new ElementNotFoundException(platformsLinkElem, PLATFORMS_KEY);
        }
        
        for (int i = 0; i < platformsElem.getChildElements().size(); i++) {
            Element idElem = platformsElem.getChildElements().get(i).getFirstChildElement(ID_KEY);
            if (idElem == null) {
                throw new ElementNotFoundException(platformsElem.getChildElements().get(i), ID_KEY);
            }
            locationData.addPlatform(Integer.parseInt(idElem.getValue()));
        }
    }
}
