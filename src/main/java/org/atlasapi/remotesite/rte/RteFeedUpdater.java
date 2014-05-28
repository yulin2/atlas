package org.atlasapi.remotesite.rte;

import static com.google.common.base.Preconditions.checkNotNull;

import com.metabroadcast.common.scheduling.ScheduledTask;


public class RteFeedUpdater extends ScheduledTask {

    private final RteFeedSupplier feedSupplier;
    private final RteFeedProcessor feedProcessor;
    
    public RteFeedUpdater(RteFeedSupplier feedSupplier, RteFeedProcessor feedProcessor) {
        this.feedSupplier = checkNotNull(feedSupplier);
        this.feedProcessor = checkNotNull(feedProcessor);
    }
    
    @Override
    protected void runTask() {
        feedProcessor.process(feedSupplier.supplyFeed(), this.reporter());
    }
    
}
