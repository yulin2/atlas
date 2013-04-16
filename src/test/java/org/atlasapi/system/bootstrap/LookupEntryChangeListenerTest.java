package org.atlasapi.system.bootstrap;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.atlasapi.equiv.EquivalenceRecord;
import org.atlasapi.equiv.EquivalenceRecordStore;
import org.atlasapi.equiv.EquivalenceRef;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.lookup.entry.LookupEntry;
import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

@RunWith(MockitoJUnitRunner.class)
public class LookupEntryChangeListenerTest {

    private final LookupEntryStore lookupStore = mock(LookupEntryStore.class);
    private final EquivalenceRecordStore equivStore = mock(EquivalenceRecordStore.class);
    private final LookupEntryChangeListener listener = 
            new LookupEntryChangeListener(1, lookupStore, equivStore);
    
    @Test
    public void testWritingReflexiveOnlyDoesntQueryTheLookupStoreAgain() {
        
        Content content = new Episode("uri", "curie", Publisher.METABROADCAST);
        content.setId(1234);
        LookupEntry entry = LookupEntry.lookupEntryFrom(content);
        
        listener.onChange(entry);
        
        verify(lookupStore, never()).entriesForCanonicalUris(ImmutableList.of("uri"));
        ArgumentCaptor<Iterable> captor = ArgumentCaptor.forClass(Iterable.class); 
        verify(equivStore).writeRecords(captor.capture());
        
        Iterable written = captor.getValue();
        EquivalenceRecord record = (EquivalenceRecord) Iterables.getOnlyElement(written);
        assertThat(record.getId(), is(content.getId()));
        assertThat(record.getPublisher(), is(content.getPublisher()));
        assertThat(record.getUpdated(), is(entry.updated()));
        assertThat(record.getCreated(), is(entry.created()));
        assertThat(record.getGeneratedAdjacents(), hasItems(record.getSelf()));
        assertThat(record.getExplicitAdjacents(), hasItems(record.getSelf()));
        assertThat(record.getEquivalents(), hasItems(record.getSelf()));
        
    }

    @Test
    public void testWritingResolvesIdsForEquivalents() {
        
        Content subject = new Episode("suri", "curie", Publisher.METABROADCAST);
        subject.setId(1234);
        LookupEntry subjEntry = LookupEntry.lookupEntryFrom(subject);

        Content equivalent = new Episode("euri", "curie", Publisher.BBC);
        equivalent.setId(1235);
        LookupEntry equivEntry = LookupEntry.lookupEntryFrom(equivalent);
        
        ImmutableList<LookupRef> refs = ImmutableList.of(equivEntry.lookupRef());
        subjEntry = subjEntry.copyWithDirectEquivalents(refs)
            .copyWithExplicitEquivalents(refs)
            .copyWithEquivalents(refs);
        
        when(lookupStore.entriesForCanonicalUris(argThat(hasItems(equivalent.getCanonicalUri()))))
            .thenReturn(ImmutableList.of(equivEntry));
        
        listener.onChange(subjEntry);
        
        verify(lookupStore, never()).entriesForCanonicalUris(ImmutableList.of(subject.getCanonicalUri()));
        verify(lookupStore, times(1)).entriesForCanonicalUris(ImmutableList.of(equivalent.getCanonicalUri()));
        ArgumentCaptor<Iterable> captor = ArgumentCaptor.forClass(Iterable.class); 
        verify(equivStore).writeRecords(captor.capture());
        
        EquivalenceRef equivRef = new EquivalenceRef(equivalent.getId(), equivalent.getPublisher());
        Iterable written = captor.getValue();
        EquivalenceRecord record = (EquivalenceRecord) Iterables.getOnlyElement(written);
        assertThat(record.getId(), is(subject.getId()));
        assertThat(record.getPublisher(), is(subject.getPublisher()));
        assertThat(record.getUpdated(), is(subjEntry.updated()));
        assertThat(record.getCreated(), is(subjEntry.created()));
        assertThat(record.getGeneratedAdjacents(), hasItems(record.getSelf(), equivRef));
        assertThat(record.getExplicitAdjacents(), hasItems(record.getSelf(), equivRef));
        assertThat(record.getEquivalents(), hasItems(record.getSelf(), equivRef));
        
    }

}
