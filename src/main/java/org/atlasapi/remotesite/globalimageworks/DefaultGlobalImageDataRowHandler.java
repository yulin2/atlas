package org.atlasapi.remotesite.globalimageworks;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.ContentMerger;
import org.atlasapi.remotesite.ContentMerger.MergeStrategy;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.base.Maybe;

public class DefaultGlobalImageDataRowHandler implements GlobalImageDataRowHandler {

    private final ContentResolver resolver;
    private final ContentWriter writer;
    private final ContentExtractor<GlobalImageDataRow, Content> extractor;
    private final ContentMerger contentMerger;
    
    public DefaultGlobalImageDataRowHandler(ContentResolver resolver, ContentWriter writer, 
            ContentExtractor<GlobalImageDataRow, Content> extractor) {
        this.resolver = checkNotNull(resolver);
        this.writer = checkNotNull(writer);
        this.extractor = checkNotNull(extractor);
        this.contentMerger = new ContentMerger(MergeStrategy.MERGE);
    }
    
    @Override
    public void handle(GlobalImageDataRow row) {
        Content content = extractor.extract(row);
        Maybe<Identified> existing = resolve(content.getCanonicalUri());
        if (existing.isNothing()) {
            write(content);
        } else {
            Identified identified = existing.requireValue();
            write(contentMerger.merge(ContentMerger.asItem(identified), (Item) content));
        }
    }
    
    private void write(Content content) {
        Item item = (Item) content;
        writer.createOrUpdate(item);
    }

    private Maybe<Identified> resolve(String uri) {
        ImmutableSet<String> uris = ImmutableSet.of(uri);
        return resolver.findByCanonicalUris(uris).get(uri);
    }

}
