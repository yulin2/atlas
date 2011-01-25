package org.atlasapi.remotesite.bbc.ion.model;

import java.net.URL;

public class IonLink {

    private String rel;
    private URL href;
    private String contentType;
    private String localeStr;

    public String getRel() {
        return rel;
    }

    public URL getHref() {
        return href;
    }

    public String getContentType() {
        return contentType;
    }

    public String getLocaleStr() {
        return localeStr;
    }

}
