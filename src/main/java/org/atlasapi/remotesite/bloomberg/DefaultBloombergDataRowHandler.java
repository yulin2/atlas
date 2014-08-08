package org.atlasapi.remotesite.bloomberg;

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

public class DefaultBloombergDataRowHandler implements BloombergDataRowHandler {

    private final ContentResolver resolver;
    private final ContentWriter writer;
    private final ContentExtractor<BloombergDataRow, Content> extractor;
    private final ContentMerger contentMerger;
    
    public DefaultBloombergDataRowHandler(ContentResolver resolver, ContentWriter writer, 
            ContentExtractor<BloombergDataRow, Content> extractor) {
        this.resolver = checkNotNull(resolver);
        this.writer = checkNotNull(writer);
        this.extractor = checkNotNull(extractor);
        this.contentMerger = new ContentMerger(MergeStrategy.MERGE);
    }
    
    @Override
    public void handle(BloombergDataRow row) {
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
