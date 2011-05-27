package org.atlasapi.query.content;

import java.util.List;
import java.util.Map;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.persistence.lookup.LookupResolver;
import org.atlasapi.persistence.lookup.entry.Equivalent;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class PublisherFilteringLookupResolver implements LookupResolver {

    private final LookupResolver delegate;

    public PublisherFilteringLookupResolver(LookupResolver delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public List<Equivalent> lookup(String id, ApplicationConfiguration config) {
        return filter(delegate.lookup(id, config), id, config);
    }

    private List<Equivalent> filter(List<Equivalent> resolvedEquivs, String id, final ApplicationConfiguration config) {
        
        Map<String, Equivalent> filteredEquivs = Maps.uniqueIndex(Iterables.filter(resolvedEquivs, new Predicate<Equivalent>() {
            @Override
            public boolean apply(Equivalent input) {
                return config.getIncludedPublishers().contains(input.publisher());
            }
        }), Equivalent.TO_ID);
        
        Equivalent equivForRequested = filteredEquivs.get(id);
        
        if(equivForRequested == null) {
            return ImmutableList.of();
        }
        
        if(!config.precedenceEnabled()) {
            return equivForRequested != null ? ImmutableList.of(equivForRequested) : ImmutableList.<Equivalent>of();
        }
        
        return ImmutableList.copyOf(filteredEquivs.values());
    }

}
