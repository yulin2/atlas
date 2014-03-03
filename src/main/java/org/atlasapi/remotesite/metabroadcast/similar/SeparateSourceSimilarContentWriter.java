package org.atlasapi.remotesite.metabroadcast.similar;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.SimilarContentRef;
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
    public void write(Content sourceContent, Iterable<SimilarContentRef> similar) {
        checkState(sourceContent != null);
        
        String writeUri = similarContentPublisherUriFor(sourceContent.getCanonicalUri());
        Maybe<Identified> content = contentResolver.findByCanonicalUris(ImmutableSet.of(writeUri)).getFirstValue();
        
        Item writeContent;
        if (content.hasValue()) {
            writeContent = (Item) content.requireValue();
        } else {
            writeContent = create(writeUri);
        }
        writeContent.setSimilarContent(similar);
        writeContent.setEquivalentTo(ImmutableSet.of(LookupRef.from(sourceContent)));
        contentWriter.createOrUpdate(writeContent);
    }
    
    private Item create(String writeUri) {
        return new Item(writeUri, null, publisher);
    }

    private String similarContentPublisherUriFor(String sourceUri) {
        return String.format("http://%s/%s", publisher.key(), sourceUri.replaceFirst("(http(s?)://)", ""));
    }
}
