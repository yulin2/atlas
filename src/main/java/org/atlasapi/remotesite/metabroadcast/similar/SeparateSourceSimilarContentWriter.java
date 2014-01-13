package org.atlasapi.remotesite.metabroadcast.similar;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.base.Maybe;


public class SeparateSourceSimilarContentWriter implements SimilarContentWriter {

    private final Publisher publisher;
    private final ContentResolver contentResolver;
    private final ContentWriter contentWriter;

    public SeparateSourceSimilarContentWriter(Publisher publisher, ContentResolver contentResolver, ContentWriter contentWriter) {
        this.contentResolver = checkNotNull(contentResolver);
        this.contentWriter = checkNotNull(contentWriter);
        this.publisher = checkNotNull(publisher);
    }
    
    @Override
    public void write(String sourceUri, Iterable<ChildRef> similar) {
        checkState(sourceUri != null);
        
        String writeUri = similarContentPublisherUriFor(sourceUri);
        Maybe<Identified> content = contentResolver.findByCanonicalUris(ImmutableSet.of(writeUri)).getFirstValue();
        
        Item writeContent;
        if (content.hasValue()) {
            writeContent = (Item) content.requireValue();
        } else {
            writeContent = create(writeUri);
        }
        writeContent.setSimilarContent(similar);
        contentWriter.createOrUpdate(writeContent);
    }
    
    private Item create(String writeUri) {
        return new Item(writeUri, null, publisher);
    }

    private String similarContentPublisherUriFor(String sourceUri) {
        return String.format("http://%s/%s", publisher.name(), sourceUri.replaceFirst("(http(s?)://)", ""));
    }
}
