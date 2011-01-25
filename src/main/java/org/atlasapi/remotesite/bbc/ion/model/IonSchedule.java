package org.atlasapi.remotesite.bbc.ion.model;

import java.util.List;

import org.joda.time.DateTime;

public class IonSchedule {

    private Integer count;
    private DateTime updated;
    private String type;
    private String id;
    private String localeStr;

    private IonLink link;
    private IonPagination pagination;
    private IonGenerator generator;
    private IonContext context;

    private List<IonBroadcast> blocklist;

    public IonSchedule() {
    }

    public Integer getCount() {
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

    public List<IonBroadcast> getBlocklist() {
        return blocklist;
    }

}
