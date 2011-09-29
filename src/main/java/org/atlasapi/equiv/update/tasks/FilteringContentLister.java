package org.atlasapi.equiv.update.tasks;

import java.util.Iterator;

import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingCriteria;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

public class FilteringContentLister implements ContentLister {

    private final Predicate<Content> filter;
    private final ContentLister generator;

    public FilteringContentLister(ContentLister generator, Predicate<Content> filter) {
        this.generator = generator;
        this.filter = filter;
    }

    @Override
    public Iterator<Content> listContent(ContentListingCriteria criteria) {
        return Iterators.filter(generator.listContent(criteria), filter);
    }
    
}
