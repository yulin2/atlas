package org.atlasapi.messaging.workers;

import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.ContentIndexer;
import org.atlasapi.media.content.ContentStore;
import org.atlasapi.media.content.IndexException;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.util.Resolved;
import org.atlasapi.messaging.EntityUpdatedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class EsIndexer extends AbstractWorker {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ContentStore contentStore;
    private final ContentIndexer contentIndexer;

    public EsIndexer(ContentStore contentResolver, ContentIndexer contentIndexer) {
        this.contentStore = contentResolver;
        this.contentIndexer = contentIndexer;
    }

    @Override
    public void process(final EntityUpdatedMessage message) {
        Futures.addCallback(resolveContent(message),
                new FutureCallback<Resolved<Content>>() {

                    @Override
                    public void onFailure(Throwable throwable) {
                        log.error("Indexing error:", throwable);
                    }

                    @Override
                    public void onSuccess(Resolved<Content> results) {
                        Optional<Content> content = results.getResources().first();
                        if (content.isPresent()) {
                            Content source = content.get();
                            log.info("Indexing {}", source);
                            try {
                                contentIndexer.index((Item) source);
                            } catch (IndexException ie) {
                                onFailure(ie);
                            }
                        } else {
                            log.warn("{}: failed to resolved {} {}",
                                    new Object[] {
                                        message.getMessageId(),
                                        message.getEntityType(),
                                        message.getEntityId() });
                        }
                    }
                }
                );
    }

    private ListenableFuture<Resolved<Content>> resolveContent(final EntityUpdatedMessage message) {
        return contentStore.resolveIds(ImmutableList.of(Id.valueOf(message.getEntityId())));
    }
}