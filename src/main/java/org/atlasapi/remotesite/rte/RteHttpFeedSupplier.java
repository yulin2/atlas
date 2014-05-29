package org.atlasapi.remotesite.rte;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.remotesite.support.atom.AtomClient;

import com.google.api.client.repackaged.com.google.common.base.Throwables;
import com.google.common.base.Supplier;
import com.sun.syndication.feed.atom.Feed;


public class RteHttpFeedSupplier implements Supplier<Feed> {

    private final AtomClient atomClient;
    private final String feedUrl;
    
    public RteHttpFeedSupplier(AtomClient atomClient, String feedUrl) {
        this.atomClient = checkNotNull(atomClient);
        this.feedUrl = checkNotNull(feedUrl);
    }

    @Override
    public Feed get() {
        try {
            return atomClient.get(feedUrl);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

}
