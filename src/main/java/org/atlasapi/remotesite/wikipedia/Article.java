package org.atlasapi.remotesite.wikipedia;

import org.joda.time.DateTime;

public abstract class Article {

    abstract DateTime getLastModified();

    abstract String getMediaWikiSource();

    abstract String getTitle();

    public String getUrl() {
        return "http://en.wikipedia.org/wiki/" + getTitle();
    }

}
