package org.atlasapi.query.content;

import static org.atlasapi.application.ApplicationConfiguration.DEFAULT_CONFIGURATION;
import static org.atlasapi.content.criteria.attribute.Attributes.DESCRIPTION_PUBLISHER;
import static org.atlasapi.content.criteria.operator.Operators.EQUALS;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.content.criteria.AtomicQuery;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.lookup.InMemoryLookupEntryStore;
import org.atlasapi.persistence.lookup.entry.LookupEntry;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

@RunWith(MockitoJUnitRunner.class)
public class LookupResolvingQueryExecutorTest {
    
    private final ContentResolver contentResolver = mock(ContentResolver.class, "contentResolver");
    private final InMemoryLookupEntryStore lookupStore = new InMemoryLookupEntryStore();
    
    private final LookupResolvingQueryExecutor executor = new LookupResolvingQueryExecutor(contentResolver, lookupStore);

    @Ignore("Doesn't currently work as equivalence isn't implemented yet in Cassandra.")
    public void setsSameAs() {
        final String query = "query";
        final Item queryItem = new Item(query, "qcurie", Publisher.BBC);
        final Item equivItem = new Item("equiv", "ecurie", Publisher.YOUTUBE);
        
        LookupEntry queryEntry = LookupEntry.lookupEntryFrom(queryItem);
        LookupEntry equivEntry = LookupEntry.lookupEntryFrom(equivItem);
        
        lookupStore.store(queryEntry
            .copyWithDirectEquivalents(ImmutableSet.of(equivEntry.lookupRef()))
            .copyWithEquivalents(ImmutableSet.of(equivEntry.lookupRef())));
        lookupStore.store(equivEntry
            .copyWithDirectEquivalents(ImmutableSet.of(queryEntry.lookupRef()))
            .copyWithEquivalents(ImmutableSet.of(queryEntry.lookupRef())));
        
        when(contentResolver.findByCanonicalUris(argThat(hasItems(queryItem.getCanonicalUri(), equivItem.getCanonicalUri()))))
            .thenReturn(ResolvedContent.builder()
                .put(queryItem.getId(), queryItem)
                .put(equivItem.getId(), equivItem)
            .build());
        
        Map<Id, List<Identified>> result = executor.executeUriQuery(ImmutableList.of(query), defaultQuery(DEFAULT_CONFIGURATION.getEnabledSources()));
        
        assertEquals(2, result.get(query).size());
        for (Identified resolved : result.get(query)) {
            if(resolved.getCanonicalUri().equals(query)) {
                assertEquals(ImmutableSet.of(LookupRef.from(equivItem)), resolved.getEquivalentTo());
            } else if(resolved.getCanonicalUri().equals("equiv")) {
                assertEquals(ImmutableSet.of(LookupRef.from(queryItem)), resolved.getEquivalentTo());
            }
        }
        
    }
    
    @Test
    public void testNoEquivalence() {
        final String query = "query";
        final Item queryItem = new Item(query, "qcurie", Publisher.BBC);
        queryItem.setId(1234L);
        final Item equivItem = new Item("equiv", "ecurie", Publisher.YOUTUBE);
        equivItem.setId(1235L);
        
        lookupStore.store(lookupEntryWithEquivalents(query, 1234L, LookupRef.from(queryItem), LookupRef.from(equivItem)));
        
        when(contentResolver.findByIds(argThat(hasItems(queryItem.getId()))))
            .thenReturn(ResolvedContent.builder()
                .put(queryItem.getId(), queryItem)
            .build());
        
        Map<Id, List<Identified>> result = executor.executeUriQuery(ImmutableList.of(query), defaultQuery(DEFAULT_CONFIGURATION.getEnabledSources()));
        
        assertEquals(1, result.size());
    }
    
    
    @Test
    public void testPublisherFiltering() {
        final String uri1 = "uri1";
        final Item item1 = new Item(uri1, "qcurie1", Publisher.BBC);
        final String uri2 = "uri2";
        final Item item2 = new Item(uri2, "qcurie1", Publisher.BBC);
        
        lookupStore.store(LookupEntry.lookupEntryFrom(item1));
        lookupStore.store(LookupEntry.lookupEntryFrom(item2));

        Map<Id, List<Identified>> result = executor.executeUriQuery(ImmutableList.of(uri1, uri2), defaultQuery(ImmutableSet.of(Publisher.PA)));
        
        verify(contentResolver, never()).findByCanonicalUris(Matchers.<Iterable<String>>any());
        assertEquals(0, result.size());
    }
    
    private LookupEntry lookupEntryWithEquivalents(String uri, Long id, LookupRef... equiv) {
        return new LookupEntry(uri, id, LookupRef.from(new Item("uri","curie",Publisher.BBC)), ImmutableSet.of(uri), ImmutableSet.<LookupRef>of(), ImmutableSet.<LookupRef>of(), ImmutableSet.copyOf(equiv), null, null);
    }

    protected ContentQuery defaultQuery(Set<Publisher> sources) {
        AtomicQuery aq = DESCRIPTION_PUBLISHER.createQuery(EQUALS, sources);
        ContentQuery cq = ContentQuery.MATCHES_EVERYTHING.copyWithOperands(ImmutableSet.of(aq));
        return cq;
    }
}
