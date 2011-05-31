package org.atlasapi.equiv.tasks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import junit.framework.TestCase;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class SymmetricItemVersionMergerTest extends TestCase {

    public void testMergeVersions() {

        SymmetricItemVersionMerger merger = new SymmetricItemVersionMerger();
        
        Item item1 = new Item("subjectUri", "subjectCurie", Publisher.PA);
        
        Version nativeVersion = new Version();
        nativeVersion.setProvider(Publisher.PA);
        nativeVersion.setCanonicalUri("nativeSubjectVersion");
        item1.addVersion(nativeVersion);
        
        Version prevEquivVersion = new Version();
        prevEquivVersion.setProvider(Publisher.BBC);
        prevEquivVersion.setCanonicalUri("prevEquivBBCVersion");
        item1.addVersion(prevEquivVersion);
        
        Version nonNativeVersion = new Version();
        nonNativeVersion.setProvider(Publisher.FIVE);
        nonNativeVersion.setCanonicalUri("nonNativeSubjectVersion");
        item1.addVersion(nonNativeVersion);
        
        Item item2 = new Item("equivUri", "equivCurie", Publisher.BBC);
        
        nativeVersion = new Version();
        nativeVersion.setProvider(Publisher.BBC);
        nativeVersion.setCanonicalUri("nativeEquivVersion");
        item2.addVersion(nativeVersion);
        
        prevEquivVersion = new Version();
        prevEquivVersion.setProvider(Publisher.PA);
        prevEquivVersion.setCanonicalUri("prevEquivPAVersion");
        item2.addVersion(prevEquivVersion);
        
        nonNativeVersion = new Version();
        nonNativeVersion.setProvider(Publisher.C4);
        nonNativeVersion.setCanonicalUri("nonNativeEquivVersion");
        item2.addVersion(nonNativeVersion);
        
        merger.mergeVersions(item1, ImmutableList.of(item2));
        
        assertThat(item1.getVersions().size(), is(4));
        assertThat(ImmutableList.copyOf(Iterables.transform(item1.getVersions(), Identified.TO_URI)), hasItems("nativeSubjectVersion", "nonNativeSubjectVersion", "nativeEquivVersion", "nonNativeEquivVersion"));
        
        assertThat(item2.getVersions().size(), is(4));
        assertThat(ImmutableList.copyOf(Iterables.transform(item2.getVersions(), Identified.TO_URI)), hasItems("nativeEquivVersion", "nonNativeEquivVersion", "nativeSubjectVersion", "nonNativeSubjectVersion"));

        merger.mergeVersions(item1, ImmutableList.of(item2));
        
        assertThat(item1.getVersions().size(), is(4));
        assertThat(ImmutableList.copyOf(Iterables.transform(item1.getVersions(), Identified.TO_URI)), hasItems("nativeSubjectVersion", "nonNativeSubjectVersion", "nativeEquivVersion", "nonNativeEquivVersion"));
        
        assertThat(item2.getVersions().size(), is(4));
        assertThat(ImmutableList.copyOf(Iterables.transform(item2.getVersions(), Identified.TO_URI)), hasItems("nativeEquivVersion", "nonNativeEquivVersion", "nativeSubjectVersion", "nonNativeSubjectVersion"));
        
        assertThat(item1.getEquivalentTo(), hasItems("equivUri"));
        assertThat(item2.getEquivalentTo(), hasItems("subjectUri"));
    }

}
