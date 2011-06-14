package org.atlasapi.query.content;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.KnownTypeContentResolver;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.lookup.LookupResolver;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class LookupResolvingQueryExecutor implements KnownTypeQueryExecutor {

    private final LookupResolver lookupResolver;
    private final KnownTypeContentResolver contentResolver;

    public LookupResolvingQueryExecutor(KnownTypeContentResolver contentResolver, LookupResolver lookupResolver) {
        this.contentResolver = contentResolver;
        this.lookupResolver = lookupResolver;
    }

    @Override
    public Map<String, List<Identified>> executeUriQuery(Iterable<String> uris, final ContentQuery query) {
        Map<String, List<Identified>> resolvedContent = Maps.newHashMapWithExpectedSize(Iterables.size(uris));
        
        for (String uri : uris) {
            List<Identified> resolvedEquivs = resolveEquivs(uri, query.getConfiguration());
            if(!resolvedEquivs.isEmpty()) {
                resolvedContent.put(uri, resolvedEquivs);
            }
        }
        
        return resolvedContent;
    }

    private List<Identified> resolveEquivs(String uri, ApplicationConfiguration config) {
        Iterable<LookupRef> equivRefs = Iterables.filter(lookupResolver.equivalentsFor(uri), enabledPublishers(config));
        if (Iterables.isEmpty(equivRefs) || !containsRequestedUri(equivRefs, uri)) {
            return ImmutableList.of();
        }
        return setEquivalentToFields(contentResolver.findByLookupRefs(equivRefs).getAllResolvedResults());
    }

    private boolean containsRequestedUri(Iterable<LookupRef> equivRefs, String uri) {
        for (LookupRef equivRef : equivRefs) {
            if(equivRef.id().equals(uri)){
                return true;
            }
        }
        return false;
    }

    private List<Identified> setEquivalentToFields(List<Identified> resolvedResults) {
        ImmutableSet<String> uris = ImmutableSet.copyOf(Iterables.transform(resolvedResults, Identified.TO_URI));
        for (Identified ided : resolvedResults) {
            ided.setEquivalentTo(Sets.difference(uris, ImmutableSet.of(ided.getCanonicalUri())));
        }
        return resolvedResults;
    }
    
    private Predicate<LookupRef> enabledPublishers(ApplicationConfiguration config) {
        final Set<Publisher> enabledPublishers = config.getIncludedPublishers();
        return new Predicate<LookupRef>() {
            @Override
            public boolean apply(LookupRef input) {
                return enabledPublishers.contains(input.publisher());
            }
        };
    }
}
