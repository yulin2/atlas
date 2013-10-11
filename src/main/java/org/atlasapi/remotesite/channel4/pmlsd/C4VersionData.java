package org.atlasapi.remotesite.channel4.pmlsd;

import java.util.Map;

import org.jdom.Element;
import org.joda.time.DateTime;


public class C4VersionData {

    private final String id;
    private final String uri;
    private final Element mediaGroup;
    private final Map<String, String> lookup;
    private final DateTime lastUpdated;

    public C4VersionData(String id, String uri, Element mediaGroup, Map<String, String> lookup, DateTime lastUpdated) {
        this.id = id;
        this.uri = uri;
        this.mediaGroup = mediaGroup;
        this.lookup = lookup;
        this.lastUpdated = lastUpdated;
    }

    public String getId() {
        return id;
    }

    public String getUri() {
        return uri;
    }

    public Element getMediaGroup() {
        return mediaGroup;
    }
    
    public Map<String, String> getLookup() {
        return lookup;
    }

    public DateTime getLastUpdated() {
        return lastUpdated;
    }
    
}
