package org.atlasapi.remotesite.bbc.nitro.v1;

import com.google.api.client.util.Key;

public class NitroGenre {

    @Key private String id;
    @Key private String type;
    @Key private String title;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof NitroGenre) {
            NitroGenre other = (NitroGenre) that;
            return id.equals(other.id);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s", id, title);
    }

}
