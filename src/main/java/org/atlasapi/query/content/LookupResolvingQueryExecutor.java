package org.atlasapi.query.content;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.DefaultEquivalentContentResolver;
import org.atlasapi.persistence.content.EquivalentContent;
import org.atlasapi.persistence.content.EquivalentContentResolver;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.lookup.entry.LookupEntryStore;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class LookupResolvingQueryExecutor implements KnownTypeQueryExecutor {

    private final EquivalentContentResolver contentResolver;

    public LookupResolvingQueryExecutor(ContentResolver contentResolver, LookupEntryStore lookupResolver) {
        this.contentResolver = new DefaultEquivalentContentResolver(contentResolver, lookupResolver);
    }

    @Override
    public Map<Id, List<Identified>> executeUriQuery(Iterable<String> uris, final ContentQuery query) {
        EquivalentContent content = contentResolver.resolveUris(uris, query.includedPublishers(), query.getAnnotations(), true);
        return transform(content);
    }
    
    @Override
    public Map<Id, List<Identified>> executeIdQuery(Iterable<Id> ids, final ContentQuery query) {
        EquivalentContent content = contentResolver.resolveIds(ids, query.includedPublishers(), query.getAnnotations());
        return transform(content);
    }

    protected Map<Id, List<Identified>> transform(EquivalentContent content) {
        return Maps.transformValues(content.asMap(), new Function<Collection<Content>, List<Identified>>() {
            @Override
            public List<Identified> apply(@Nullable Collection<Content> input) {
                return ImmutableSet.copyOf(Iterables.filter(input, Identified.class)).asList();
            }
        });
    }

}
