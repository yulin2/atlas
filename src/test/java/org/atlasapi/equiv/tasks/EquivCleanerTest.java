package org.atlasapi.equiv.tasks;

import static org.atlasapi.media.entity.Publisher.BBC;
import static org.atlasapi.media.entity.Publisher.C4;
import static org.atlasapi.media.entity.Publisher.FIVE;
import static org.atlasapi.media.entity.Publisher.PA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import java.util.Set;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.jmock.Expectations;
import org.jmock.Mockery;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class EquivCleanerTest extends TestCase {
    
    private final Mockery context = new Mockery();
    private final ContentResolver resolver = context.mock(ContentResolver.class);
    
    private final EquivCleaner cleaner = new EquivCleaner(resolver);
    
    public void testCleanEquivalencesForBrandsWithNoItems() {
        //subject brand
        final Brand one = new Brand("testUri1", "testCurie1", PA);
        //ignored (so retained) equivalent
        final Brand two = new Brand("testUri2", "testCurie2", FIVE);
        
        //old equivalent which get removed
        final Brand three = new Brand("testUri3", "testCurie3", BBC);
        //equivalent of three, to which three should still be equivalent after cleaning.
        final Brand four = new Brand("testUri4", "testCurie4", C4);
        
        //retained equivalent of one because it's a known equivalent
        final Brand five = new Brand("testUri5", "testCurie5", BBC);
        
        one.addEquivalentTo(two);
        one.addEquivalentTo(three);
        one.addEquivalentTo(five);
        
        three.addEquivalentTo(one);
        three.addEquivalentTo(four);
        
        context.checking(new Expectations(){{
            one(resolver).findByCanonicalUri("testUri2"); will(returnValue(two));
            one(resolver).findByCanonicalUri("testUri3"); will(returnValue(three));
        }});
        
        Set<Identified> toWrite = cleaner.cleanEquivalences(one, ImmutableSet.of(five), ImmutableSet.of(BBC));

        assertThat(one.getEquivalentTo().size(), is(2));
        assertThat(one.getEquivalentTo(), hasItem(two.getCanonicalUri()));
        assertThat(one.getEquivalentTo(), hasItem(five.getCanonicalUri()));
        
        assertThat(three.getEquivalentTo().size(), is(1));
        assertThat(three.getEquivalentTo(), hasItem(four.getCanonicalUri()));
        
        assertThat(toWrite.size(), is(1));
        assertThat(toWrite, hasItem((Identified)three));
        
        context.assertIsSatisfied();
    }
    
    public void testCleanVersionsForItemsInBrand() {
        
        final Item subject = new Item("subjectUri", "subjectCurie", PA);
        
        //Items own version
        Version subjectNativeVersion = new Version();
        subjectNativeVersion.setProvider(PA);
        subject.addVersion(subjectNativeVersion);

        //Version from an old equiv item, this should go
        Version subjectOldEquivVersion = new Version();
        subjectOldEquivVersion.setProvider(BBC);
        subject.addVersion(subjectOldEquivVersion);
        
        //Version from a confirmed equiv item
        Version subjectStillEquivVersion = new Version();
        subjectStillEquivVersion.setProvider(C4);
        subject.addVersion(subjectStillEquivVersion);
        
        //Version for an equiv item from an ignored publisher
        Version subjectNonNativeVersion = new Version();
        subjectNonNativeVersion.setProvider(FIVE);
        subject.addVersion(subjectNonNativeVersion);
        
        final Item oldEquivItem = new Item("oldEquivUri", "oldEquivCurie", BBC);
        
        //Items own version
        Version oldEquivNativeVersion = new Version();
        oldEquivNativeVersion.setProvider(BBC);
        oldEquivItem.addVersion(oldEquivNativeVersion);

        //Version from an old equiv item (the subject item)
        Version oldEquivOldEquivVersion = new Version();
        oldEquivOldEquivVersion.setProvider(PA);
        oldEquivItem.addVersion(oldEquivOldEquivVersion);
        
        final Item confirmedEquivItem = new Item("conEquivUri", "conEquivCurie", C4);

        //Version from a confirmed equiv item
        Version confirmedEquivNativeVersion = new Version();
        confirmedEquivNativeVersion.setProvider(C4);
        confirmedEquivItem.addVersion(confirmedEquivNativeVersion);
        
        //Version from an confirmed equiv item (the subject item)
        Version confirmedEquivVersion = new Version();
        confirmedEquivVersion.setProvider(PA);
        confirmedEquivItem.addVersion(confirmedEquivVersion);
        
        subject.addEquivalentTo(oldEquivItem);
        oldEquivItem.addEquivalentTo(subject);
        
        subject.addEquivalentTo(confirmedEquivItem);
        confirmedEquivVersion.addEquivalentTo(subject);
        
        context.checking(new Expectations(){{
            one(resolver).findByCanonicalUri("oldEquivUri"); will(returnValue(oldEquivItem));
        }});
        
        Set<Identified> toWrite = cleaner.cleanEquivalences(subject, ImmutableSet.of(confirmedEquivItem), ImmutableSet.of(BBC, C4));
        
        assertThat(subject.getVersions().size(), is(3));
        assertThat(subject.getVersions(), hasItems(subjectNativeVersion, subjectNonNativeVersion, subjectStillEquivVersion));
        
        assertThat(oldEquivItem.getVersions().size(), is(1));
        assertThat(Iterables.getOnlyElement(oldEquivItem.getVersions()), is(equalTo(oldEquivNativeVersion)));
        
        assertThat(confirmedEquivItem.getVersions().size(), is(2));
        assertThat(confirmedEquivItem.getVersions(), hasItems(confirmedEquivNativeVersion, confirmedEquivVersion));
        
        assertThat(toWrite, hasItem((Identified)oldEquivItem));
    }
}
