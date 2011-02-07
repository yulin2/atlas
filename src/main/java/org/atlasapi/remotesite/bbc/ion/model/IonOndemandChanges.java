package org.atlasapi.remotesite.bbc.ion.model;

import java.util.List;

import org.joda.time.DateTime;

public class IonOndemandChanges {

    private Long count;
    private DateTime updated;
    private String type;
    private String id;
    private String localeStr;
    private DateTime nextFromDatetime;

    private IonLink link;
    private IonPagination pagination;
    private IonGenerator generator;
    private IonContext context;

    private List<IonOndemandChange> blocklist;

    public IonOndemandChanges() {
    }

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

    public List<IonOndemandChange> getBlocklist() {
        return blocklist;
    }

    public DateTime nextFromDatetime() {
        return nextFromDatetime;
    }
}
