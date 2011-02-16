package org.atlasapi.remotesite.bbc.ion.model;

import org.joda.time.DateTime;

public abstract class IonFeed {

    private Long count;
    private DateTime updated;
    private String type;
    private String id;
    private String localeStr;
    private IonLink link;
    private IonPagination pagination;
    private IonGenerator generator;
    private IonContext context;
    
    public Long getCount() {
        return count;
    }

    public DateTime getUpdated() {
        return updated;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getLocaleStr() {
        return localeStr;
    }

    public IonLink getLink() {
        return link;
    }

    public IonPagination getPagination() {
        return pagination;
    }

    public IonGenerator getGenerator() {
        return generator;
    }

    public IonContext getContext() {
        return context;
    }

}
