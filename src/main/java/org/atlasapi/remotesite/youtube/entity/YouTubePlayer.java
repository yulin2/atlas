package org.atlasapi.remotesite.youtube.entity;

public class YouTubePlayer {
    String defaultUrl;
    String mobileUrl;

    public String getDefaultUrl() {
        return defaultUrl;
    }

    public String getMobileUrl() {
        return mobileUrl;
    }

    public void setMobileUrl(String mobileUrl) {
        this.mobileUrl = mobileUrl;
    }

    public void setDefaultUrl(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

}
