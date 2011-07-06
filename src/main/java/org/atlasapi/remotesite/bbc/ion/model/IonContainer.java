package org.atlasapi.remotesite.bbc.ion.model;

import java.net.URL;

import org.joda.time.DateTime;

public class IonContainer {

    private String mediumSynopsis;
    private Boolean isEmbargoed;
    private URL myAtomFeed;
    private Long position;
    private String masterbrand;
    private String parentId;
    private String longSynopsis;
    private DateTime updated;
    private String id;
    private Boolean isSimulcast;
    private String shortSynopsis;
    private String type;
    private String title;
    
    public String getMediumSynopsis() {
        return mediumSynopsis;
    }
    public Boolean getIsEmbargoed() {
        return isEmbargoed;
    }
    public URL getMyAtomFeed() {
        return myAtomFeed;
    }
    public Long getPosition() {
        return position;
    }
    public String getMasterbrand() {
        return masterbrand;
    }
    public String getParentId() {
        return parentId;
    }
    public String getLongSynopsis() {
        return longSynopsis;
    }
    public DateTime getUpdated() {
        return updated;
    }
    public String getId() {
        return id;
    }
    public Boolean getIsSimulcast() {
        return isSimulcast;
    }
    public String getShortSynopsis() {
        return shortSynopsis;
    }
    public String getType() {
        return type;
    }
    public String getTitle() {
        return title;
    }
    
}
