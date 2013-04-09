package org.atlasapi.remotesite.youview;

import nu.xom.Element;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.ScheduleEntry.ItemRefAndBroadcast;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.remotesite.ContentMerger;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;


public class DefaultYouViewElementProcessor implements YouViewElementProcessor {

    private final YouViewContentExtractor extractor;
    private final ContentResolver resolver;
    private final ContentWriter writer;
    
    public DefaultYouViewElementProcessor(YouViewContentExtractor extractor, ContentResolver resolver, ContentWriter writer) {
        this.extractor = extractor;
        this.resolver = resolver;
        this.writer = writer;
    }
    
    @Override
    public ItemRefAndBroadcast process(Element element) {
        Item item = extractor.extract(element);
        Maybe<Identified> existing = resolve(item.getCanonicalUri());
        if (existing.isNothing()) {
            write(item);
        } else {
            Identified identified = existing.requireValue();
                write(ContentMerger.merge(ContentMerger.asItem(identified), item));
        }
        return new ItemRefAndBroadcast(item, getBroadcastFromItem(item));
    }

    private Broadcast getBroadcastFromItem(Item item) {
        Version version = Iterables.getOnlyElement(item.getVersions());
        return Iterables.getOnlyElement(version.getBroadcasts());
    }

    private Maybe<Identified> resolve(String uri) {
        ImmutableSet<String> uris = ImmutableSet.of(uri);
        return resolver.findByCanonicalUris(uris).getFirstValue();
    }

    private void write(Content content) {
        writer.createOrUpdate((Item) content);
    }
}
