package org.atlasapi.remotesite.bbc.ion.ondemand;

import static org.atlasapi.media.entity.Publisher.BBC;

import java.util.concurrent.Callable;

import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.ContentStore;
import org.atlasapi.media.entity.Alias;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChange;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.collect.OptionalMap;

public class BbcIonOndemandChangeTaskBuilder {

    private final ContentStore store;
    private final AdapterLog log;

    private final BbcIonOndemandItemUpdater itemUpdater = new BbcIonOndemandItemUpdater();

    public BbcIonOndemandChangeTaskBuilder(ContentStore store, AdapterLog log) {
        this.store = store;
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
            Alias episodeUrl = new Alias("bbc:programmes:url", BbcFeeds.slashProgrammesUriForPid(change.getEpisodeId()));
            try {
                OptionalMap<Alias, Content> resolvedItem = store.resolveAliases(ImmutableList.of(episodeUrl), BBC);
                if (resolvedItem.get(episodeUrl).isPresent()) {
                    Item item = (Item) resolvedItem.get(episodeUrl).get();
                    itemUpdater.updateItemDetails(item, change);
                    store.writeContent(item);
                }/* else {
                    log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription("No item %s for on-demand change", uri));
                }*/
            } catch (Exception e) {
                log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withCause(e).withDescription("Unable to process ondemand changes for item " + episodeUrl));
            }
            return null;
        }

    }

}
