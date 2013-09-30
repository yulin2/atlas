package org.atlasapi.remotesite.pa.features;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.testing.ComplexItemTestDataBuilder;
import org.atlasapi.media.entity.testing.ItemTestDataBuilder;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class PaIdentifiedComparatorTest {

    @Before
    public void setUp() {
        System.out.println("hello, world!");
    }
    @Test
    public void testProgrammeIdLessThanFilmLessThanOthers() {
        
        Item item1 = itemWithUri("http://pressassociation.com/12354");
        Item item2 = itemWithUri("http://pressassociation.com/films/12354");
        Item item3 = itemWithUri("http://pressassociation.com/12355");
        Item item4 = itemWithUri("http://pressassociation.com/episodes/12354");
        Item item5 = itemWithUri("http://pressassociation.com/12359");
        
        ItemTestDataBuilder.item().withUri("http://pressassociation.com/12354").build();
        List<Item> ids = Lists.newArrayList(item1, item2, item3, item4, item5);
        Collections.sort(ids, new PaIdentifiedComparator());
        
        List<Item> expected = Lists.newArrayList(item2, item1, item3, item5, item4);
        assertEquals(expected, ids);
    }
    
    private Item itemWithUri(String uri) {
        return ComplexItemTestDataBuilder.complexItem().withUri(uri).build();
    }
 }
