package org.atlasapi.query.content;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.KnownTypeContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.lookup.entry.LookupEntry;
import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;

public class LookupResolvingQueryExecutor implements KnownTypeQueryExecutor {

    private final Logger log = LoggerFactory.getLogger(LookupResolvingQueryExecutor.class);
    private final KnownTypeContentResolver cassandraContentResolver;
    private final KnownTypeContentResolver mongoContentResolver;
    private final LookupEntryStore mongoLookupResolver;

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
            public boolean apply(LookupEntry input) {
                return configuration.isEnabled(input.lookupRef().publisher());
            }
        }), LookupEntry.TO_ID);

        Map<String, Set<LookupRef>> lookupRefs = Maps.transformValues(lookup, LookupEntry.TO_EQUIVS);

        Iterable<LookupRef> filteredRefs = Iterables.filter(Iterables.concat(lookupRefs.values()), enabledPublishers(configuration));

        if (Iterables.isEmpty(filteredRefs)) {
            return ImmutableMap.of();
        }


        final ResolvedContent allResolvedResults = mongoContentResolver.findByLookupRefs(filteredRefs);

        return Maps.transformEntries(lookup, new EntryTransformer<String, LookupEntry, List<Identified>>() {

            @Override
            public List<Identified> transformEntry(String uri, LookupEntry entry) {
                if (!containsRequestedUri(entry.equivalents(), uri)) {
                    return ImmutableList.of();
                }
                List<Identified> identifieds = ImmutableList.copyOf(Iterables.filter(Iterables.transform(entry.equivalents(), new Function<LookupRef, Identified>() {

                    @Override
                    public Identified apply(LookupRef input) {
                        return allResolvedResults.get(input.id()).valueOrNull();
                    }
                }), Predicates.notNull()));
                
                if (!entry.created().equals(entry.updated())) {
                    for (Identified ided : identifieds) {
                        ided.setEquivalenceUpdate(entry.updated());
                    }
                }

                return setEquivalentToFields(identifieds);
            }
        });
    }

}
