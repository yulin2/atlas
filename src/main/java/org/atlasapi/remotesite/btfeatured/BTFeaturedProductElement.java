package org.atlasapi.remotesite.btfeatured;

import nu.xom.Element;


public class BTFeaturedProductElement extends Element {

    private static final String SERIES_ELEM = "series";
    private static final String COLLECTION_ELEM = "collection";
    private static final String ID_ATTR = "id";
    private static final String TITLE_ATTR = "title";
    private static final String ASSET_ELEM = "asset";
    
    public BTFeaturedProductElement(String name, String namespace) {
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
}
