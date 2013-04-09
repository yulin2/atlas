package org.atlasapi.remotesite.bbc.products;

import org.atlasapi.media.product.ProductStore;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.s3.DefaultS3Client;
import org.atlasapi.s3.S3Client;

import com.google.common.base.Throwables;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.time.SystemClock;
import com.metabroadcast.common.time.Timestamp;
import com.metabroadcast.common.time.Timestamper;

class BbcProductsUpdater extends ScheduledTask {

    static final String S3_BUCKET = "bbc-products";

    private final Timestamper timestamper = new SystemClock();
    private final ProductStore productStore;
    private final AdapterLog log;
    private S3Client client;

    public BbcProductsUpdater(ProductStore productStore, AdapterLog log, String s3Access, String s3Secret) {
        this.productStore = productStore;
        this.log = log;
        this.client = new DefaultS3Client(s3Access, s3Secret, S3_BUCKET);
    }

    @Override
    public void runTask() {
        try {
            Timestamp start = timestamper.timestamp();
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.INFO).withDescription("BBC Products update started!").withSource(getClass()));

            new BbcProductsProcessor().process(client, productStore);

            Timestamp end = timestamper.timestamp();
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.INFO).withDescription("BBC Products update completed in " + start.durationTo(end).getStandardSeconds() + " seconds").withSource(getClass()));
        } catch (Exception e) {
            log.record(new AdapterLogEntry(AdapterLogEntry.Severity.ERROR).withCause(e).withSource(getClass()).withDescription("Exception when processing BBC Products."));
            Throwables.propagate(e);
        }
    }
}
