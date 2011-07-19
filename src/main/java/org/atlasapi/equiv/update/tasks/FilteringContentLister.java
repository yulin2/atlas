package org.atlasapi.equiv.update.tasks;

import java.util.Set;

import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.content.ContentTable;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingCriteria;
import org.atlasapi.persistence.content.listing.ContentListingHandler;
import org.atlasapi.persistence.content.listing.ContentListingProgress;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class FilteringContentLister implements ContentLister {

    private final Predicate<Content> filter;
    private final ContentLister generator;

    public FilteringContentLister(ContentLister generator, Predicate<Content> filter) {
        this.generator = generator;
        this.filter = filter;
    }
    
    @Override
    public boolean listContent(Set<ContentTable> tables, ContentListingCriteria criteria, ContentListingHandler handler) {
        return this.generator.listContent(tables, criteria, wrapWithFilter(handler));
    }

    private ContentListingHandler wrapWithFilter(final ContentListingHandler handler) {
        return new ContentListingHandler() {
            @Override
            public boolean handle(Iterable<? extends Content> contents, ContentListingProgress progress) {
                
                Iterable<? extends Content> filteredContent = Iterables.filter(contents, filter);
                if(!Iterables.isEmpty(filteredContent)) {
                    return handler.handle(filteredContent, progress);
                }
                return true;
            }
        };
    }
}
