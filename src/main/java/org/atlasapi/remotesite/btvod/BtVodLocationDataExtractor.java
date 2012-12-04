package org.atlasapi.remotesite.btvod;

import nu.xom.Element;

import static org.atlasapi.remotesite.btvod.BtVodItemDataExtractor.getChildElement;
import static org.atlasapi.remotesite.btvod.BtVodItemDataExtractor.getLinkElement;
import static org.atlasapi.remotesite.btvod.BtVodItemDataExtractor.ID_KEY;
import org.atlasapi.remotesite.btvod.model.BtVodLocationData;
import org.atlasapi.remotesite.btvod.model.BtVodLocationData.BtVodLocationDataBuilder;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class BtVodLocationDataExtractor {
    
    private static final String FROM_KEY = "from";
    private static final String TO_KEY = "to";
    private static final String PLATFORMS_KEY = "platforms";
    
    private final DateTimeFormatter dateFormatter = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();

    public BtVodLocationData extract(Element source, int duration) {
        BtVodLocationDataBuilder locationData = BtVodLocationData.builder();
        
        locationData.setUri(getChildElement(source, ID_KEY).getValue());
        
        locationData.setAvailabilityStart(dateFormatter.parseDateTime(getChildElement(source, FROM_KEY).getValue()));
        locationData.setAvailabilityEnd(dateFormatter.parseDateTime(getChildElement(source, TO_KEY).getValue()));
        
        locationData.setDuration(duration);
        
        extractPlatforms(locationData, source);
        
        return locationData.build();
    }

    private void extractPlatforms(BtVodLocationDataBuilder locationData, Element source) {
        Element platformsElem = getChildElement(getLinkElement(source, PLATFORMS_KEY), PLATFORMS_KEY);
        
        for (int i = 0; i < platformsElem.getChildElements().size(); i++) {
            Element platformElement = platformsElem.getChildElements().get(i);
            locationData.addPlatform(Integer.parseInt(getChildElement(platformElement, ID_KEY).getValue()));
        }
    }
}
