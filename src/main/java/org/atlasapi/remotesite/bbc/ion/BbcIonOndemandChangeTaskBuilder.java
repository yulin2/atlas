package org.atlasapi.remotesite.bbc.ion;

import java.util.concurrent.Callable;

import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChange;

import com.google.common.collect.ImmutableList;

public class BbcIonOndemandChangeTaskBuilder {

    public final static String SLASH_PROGRAMMES_BASE = "http://www.bbc.co.uk/programmes/";

    private final ContentResolver resolver;
    private final ContentWriter writer;
    private final AdapterLog log;

    private final BbcIonOndemandItemUpdater itemUpdater = new BbcIonOndemandItemUpdater();

    public BbcIonOndemandChangeTaskBuilder(ContentResolver resolver, ContentWriter writer, AdapterLog log) {
        this.resolver = resolver;
        this.writer = writer;
        this.log = log;
    }

    public BbcIonOndemandChangeTask taskForChange(IonOndemandChange change) {
        return new BbcIonOndemandChangeTask(change);
    }

    public class BbcIonOndemandChangeTask implements Callable<Void> {

        private final IonOndemandChange change;

        public BbcIonOndemandChangeTask(IonOndemandChange change) {
            this.change = change;
        }

        @Override
        public Void call() {
            String uri = SLASH_PROGRAMMES_BASE + change.getEpisodeId();
            try {
                ResolvedContent resolvedItem = resolver.findByCanonicalUris(ImmutableList.of(uri));
                if (resolvedItem.resolved(uri)) {
                    Item item = (Item) resolvedItem.get(uri).requireValue();
                    itemUpdater.updateItemDetails(item, change);
                    writer.createOrUpdate(item);
                }/* else {
                    log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription("No item %s for on-demand change", uri));
                }*/
            } catch (Exception e) {
                log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withCause(e).withDescription("Unable to process ondemand changes for item " + uri));
            }
            return null;
        }

    }

}
