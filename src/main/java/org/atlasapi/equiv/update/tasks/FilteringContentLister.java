package org.atlasapi.equiv.update.tasks;

import java.util.Set;

import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.content.ContentTable;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingCriteria;
import org.atlasapi.persistence.content.listing.ContentListingHandler;
import org.atlasapi.persistence.content.listing.ContentListingProgress;

import com.google.common.base.Predicate;

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
            public boolean handle(Content content, ContentListingProgress progress) {
                if(filter.apply(content)) {
                    return handler.handle(content, progress);
                }
                return true;
            }
        };
    }

}
