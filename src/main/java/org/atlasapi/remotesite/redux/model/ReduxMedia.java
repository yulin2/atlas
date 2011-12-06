package org.atlasapi.remotesite.redux.model;

public class ReduxMedia {

    private String type;
    private String uri;
    private String title;
    private String ext;
    private String kind;
    private Integer width;
    private Integer height;
    private String vcodec;
    private String acodec;
    private Integer bitrate;
    
    public ReduxMedia() { }

    public ReduxMedia(String type, String uri, String title, String ext, String kind, Integer width, Integer height, String vcodec, String acodec, Integer bitrate) {
        this.type = type;
        this.uri = uri;
        this.title = title;
        this.ext = ext;
        this.kind = kind;
        this.width = width;
        this.height = height;
        this.vcodec = vcodec;
        this.acodec = acodec;
        this.bitrate = bitrate;
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

    public String getKind() {
        return this.kind;
    }

    public Integer getWidth() {
        return this.width;
    }

    public Integer getHeight() {
        return this.height;
    }

    public String getVcodec() {
        return this.vcodec;
    }

    public String getAcodec() {
        return this.acodec;
    }

    public Integer getBitrate() {
        return this.bitrate;
    }
    
}
