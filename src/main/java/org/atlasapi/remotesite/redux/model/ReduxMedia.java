package org.atlasapi.remotesite.redux.model;

public class ReduxMedia {

    private String type;
    private String uri;
    private String title;
    private String ext;
    
    public ReduxMedia() { }

    public ReduxMedia(String type, String uri, String title, String ext) {
        this.type = type;
        this.uri = uri;
        this.title = title;
        this.ext = ext;
    }

    public String getType() {
        return type;
    }

    public String getUri() {
        return uri;
    }

    public String getTitle() {
        return title;
    }

    public String getExt() {
        return ext;
    }
    
}
