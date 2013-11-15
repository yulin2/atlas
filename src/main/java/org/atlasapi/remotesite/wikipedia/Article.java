package org.atlasapi.remotesite.wikipedia;

import org.joda.time.DateTime;

public abstract class Article {

    abstract DateTime getLastModified();

    abstract String getMediaWikiSource();

    abstract String getTitle();

    public String getUrl() {
        String safeTitle = getTitle().replaceAll(" ", "_")
                .replaceAll("\"", "%22").replaceAll("'", "%27")
                .replaceAll(",", "%2C").replaceAll(";", "%3B")
                .replaceAll("<", "%3C").replaceAll(">", "%3E")
                .replaceAll("\\[", "%5B").replaceAll("]", "%5D")
                .replaceAll("\\?", "%3F");
        // see: http://en.wikipedia.org/wiki/Help:URL#URLs_of_Wikipedia_pages
        return "http://en.wikipedia.org/wiki/" + safeTitle;
    }

}
