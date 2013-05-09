package org.atlasapi.system;

import org.atlasapi.equiv.EquivalenceRecordStore;
import org.atlasapi.persistence.lookup.entry.LookupEntry;
import org.atlasapi.persistence.lookup.entry.LookupEntryStore;
import org.atlasapi.system.bootstrap.LookupEntryChangeListener;

import com.google.common.collect.ImmutableSet;


public class LookupEntryReadWriter {

    private final LookupEntryChangeListener changeListener;
    private final LookupEntryStore lookupStore;

    public LookupEntryReadWriter(LookupEntryStore lookupStore,
            EquivalenceRecordStore equivalenceRecordStore) {
        this.lookupStore = lookupStore;
        this.changeListener = new LookupEntryChangeListener(1, lookupStore, equivalenceRecordStore);
    }

    public void onMessage(String message) {
        Iterable<LookupEntry> entries = lookupStore.entriesForCanonicalUris(ImmutableSet.of(message));
        changeListener.onChange(entries);
    }
    
}
