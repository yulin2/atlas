package org.atlasapi.remotesite.bbc.ion.model;

import org.joda.time.DateTime;

public class IonFormat {

    private DateTime updated;
    private String type;
    private String title;
    private String id;

    public DateTime getUpdated() {
        return this.updated;
    }

    public void setUpdated(DateTime updated) {
        this.updated = updated;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
