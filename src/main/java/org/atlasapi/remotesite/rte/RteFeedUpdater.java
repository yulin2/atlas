package org.atlasapi.remotesite.rte;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Supplier;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.sun.syndication.feed.atom.Feed;


public class RteFeedUpdater extends ScheduledTask {

    private final Supplier<Feed> feedSupplier;
    private final RteFeedProcessor feedProcessor;
    
    public RteFeedUpdater(Supplier<Feed> feedSupplier, RteFeedProcessor feedProcessor) {
        this.feedSupplier = checkNotNull(feedSupplier);
        this.feedProcessor = checkNotNull(feedProcessor);
    }
    
    @Override
    protected void runTask() {
        feedProcessor.process(feedSupplier.get(), this.reporter());
    }
    
}
