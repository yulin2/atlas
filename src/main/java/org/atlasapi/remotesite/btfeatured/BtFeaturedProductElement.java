package org.atlasapi.remotesite.btfeatured;

import nu.xom.Element;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class BtFeaturedProductElement extends Element {

    private static final String AVAILABILITY_ATTR = "availability";
    private static final String SERIES_ELEM = "series";
    private static final String COLLECTION_ELEM = "collection";
    private static final String ID_ATTR = "id";
    private static final String TITLE_ATTR = "title";
    private static final String ASSET_ELEM = "asset";
    
    private static final DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS"); //2013-11-30T23:59:00.0000000

    public BtFeaturedProductElement(String name, String namespace) {
        super(name, namespace);
    }

    public String getTitle() {
        return getAttributeValue(TITLE_ATTR);
    }
    
    public boolean isCollection() {
        return getFirstChildElement(COLLECTION_ELEM) != null;
    }

    public boolean isSeries() {
        return getFirstChildElement(SERIES_ELEM) != null;
    }

    public String getProductId() {
        return getAttributeValue(ID_ATTR);
    }

    public Element getContainer() {
        Element collection = getFirstChildElement(COLLECTION_ELEM);
        if (collection != null) {
            return collection;
        }
        Element series = getFirstChildElement(SERIES_ELEM);
        if (series != null) {
            return series;
        }
        throw new RuntimeException("Element is not a container");
    }
    
    public String toString() {
        if (isSeries()) {
            return "Series :"+getTitle();
        }
        else if (isCollection()) {
            return "Collection "+getTitle();
        }
        return "Product "+getTitle();
    }

    public Element getCollection() {
        return getFirstChildElement(COLLECTION_ELEM);
    }
    
    public Element getSeries() {
        return getFirstChildElement(SERIES_ELEM);
    }

    public Element getAsset() {
        return getFirstChildElement(ASSET_ELEM);
    }

    public DateTime getAvailabilityStart() {
        String availString = getAttributeValue(AVAILABILITY_ATTR);
        int end = availString.indexOf('~');
        if (end > 0) {
            availString = availString.substring(0, end);
        }
        return DateTime.parse(availString, dateFormat);
    }
 
    public DateTime getAvailabilityEnd() {
        String availString = getAttributeValue(AVAILABILITY_ATTR);
        int start = availString.indexOf('~');
        if (start > 0 && start < availString.length()) {
            availString = availString.substring(start+1);
        }
        return DateTime.parse(availString, dateFormat);
    }
}
