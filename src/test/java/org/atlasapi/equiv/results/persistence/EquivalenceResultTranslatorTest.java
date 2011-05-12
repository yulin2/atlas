package org.atlasapi.equiv.results.persistence;

import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.EquivalenceResultBuilder;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.equiv.results.combining.AddingEquivalenceCombiner;
import org.atlasapi.equiv.results.marking.TopEquivalenceMarker;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table.Cell;
import com.mongodb.DBObject;

public class EquivalenceResultTranslatorTest extends TestCase {

    private final EquivalenceResultTranslator translator = new EquivalenceResultTranslator();
    private final EquivalenceResultBuilder<Item> resultBuilder = EquivalenceResultBuilder.<Item>from(AddingEquivalenceCombiner.<Item>create(), TopEquivalenceMarker.<Item>create());

    public final Item target = target("target", "Target", Publisher.BBC);
    public final Item equivalent1 = target("equivalent1", "Equivalent1", Publisher.BBC);
    public final Item equivalent2 = target("equivalent2", "Equivalent2", Publisher.BBC);
    public final Item equivalent3 = target("equivalent3", "Equivalent3", Publisher.C4);
    
    private Item target(String name, String title, Publisher publisher) {
        Item target = new Item(name+"Uri", name+"Curie", publisher);
        target.setTitle("Test " + title);
        return target;
    }
    
    public void testCodecForEmptyResult() {
        
        List<ScoredEquivalents<Item>> scores = ImmutableList.of();
        EquivalenceResult<Item> itemResult = resultBuilder.resultFor(target, scores);
        
        DBObject dbo = translator.toDBObject(itemResult);
        RestoredEquivalenceResult restoredResult = translator.fromDBObject(dbo);
        
        assertEquals(target.getCanonicalUri(), restoredResult.id());
        assertEquals(target.getTitle(), restoredResult.title());
        
        assertTrue(restoredResult.sourceResults().isEmpty());
        
    }

    public void testCodecForTrivialResult() {
        
        List<ScoredEquivalents<Item>> scores = ImmutableList.of(
                ScoredEquivalents.<Item>fromSource("source1").addEquivalent(equivalent1, 5.0).build()
        );
        
        EquivalenceResult<Item> itemResult = resultBuilder.resultFor(target, scores);
        
        DBObject dbo = translator.toDBObject(itemResult);
        RestoredEquivalenceResult restoredResult = translator.fromDBObject(dbo);
        
        assertEquals(target.getCanonicalUri(), restoredResult.id());
        assertEquals(target.getTitle(), restoredResult.title());
        
        assertFalse(restoredResult.sourceResults().isEmpty());
        
        Set<Cell<EquivalenceIdentifier, String, Double>> cells = restoredResult.sourceResults().cellSet();
        assertEquals(1, cells.size());
        assertEquals(5.0, Iterables.getOnlyElement(cells).getValue());
        assertEquals(equivalent1.getCanonicalUri(), Iterables.getOnlyElement(cells).getRowKey().id());
        assertEquals("source1", Iterables.getOnlyElement(cells).getColumnKey());
        
        assertEquals(1, restoredResult.combinedResults().size());
        assertEquals(equivalent1.getCanonicalUri(), Iterables.getOnlyElement(restoredResult.combinedResults().entrySet()).getKey().id());
        assertEquals(5.0, Iterables.getOnlyElement(restoredResult.combinedResults().entrySet()).getValue());
        assertTrue(Iterables.getOnlyElement(restoredResult.combinedResults().entrySet()).getKey().strong());
        
    }
    
    public void testCodecForSinglePublisherResult() {
        
        List<ScoredEquivalents<Item>> scores = ImmutableList.of(
                ScoredEquivalents.<Item>fromSource("source1").addEquivalent(equivalent1, 5.0).addEquivalent(equivalent2, 5.0).addEquivalent(equivalent1, 5.0).build(),
                ScoredEquivalents.<Item>fromSource("source2").addEquivalent(equivalent1, 5.0).build()
        );
        
        EquivalenceResult<Item> itemResult = resultBuilder.resultFor(target, scores);
        
        DBObject dbo = translator.toDBObject(itemResult);
        RestoredEquivalenceResult restoredResult = translator.fromDBObject(dbo);
        
        assertEquals(target.getCanonicalUri(), restoredResult.id());
        assertEquals(target.getTitle(), restoredResult.title());
        
        assertFalse(restoredResult.sourceResults().isEmpty());
        
        EquivalenceIdentifier id1 = new EquivalenceIdentifier(equivalent1.getCanonicalUri(), equivalent1.getTitle(), true, equivalent1.getPublisher().title());
        assertEquals(10.0, restoredResult.sourceResults().get(id1, "source1"));
        assertEquals(5.0, restoredResult.sourceResults().get(id1, "source2"));

        EquivalenceIdentifier id2 = new EquivalenceIdentifier(equivalent2.getCanonicalUri(), equivalent2.getTitle(), false, equivalent2.getPublisher().title());
        assertEquals(5.0, restoredResult.sourceResults().get(id2, "source1"));
        assertNull(restoredResult.sourceResults().get(id2, "source2"));
        
        assertEquals(2, restoredResult.combinedResults().size());
        assertEquals(restoredResult.combinedResults(), ImmutableMap.of(
                id1, 15.0, 
                id2, 5.0
        ));
    }
    
    public void testCodecForTwoPublisherResult() {
        
        List<ScoredEquivalents<Item>> scores = ImmutableList.of(
                ScoredEquivalents.<Item>fromSource("source1").addEquivalent(equivalent1, 5.0).addEquivalent(equivalent2, 5.0).addEquivalent(equivalent3, 5.0).addEquivalent(equivalent1, 5.0).build(),
                ScoredEquivalents.<Item>fromSource("source2").addEquivalent(equivalent1, 5.0).addEquivalent(equivalent3, 5.0).build(),
                ScoredEquivalents.<Item>fromSource("source3").addEquivalent(equivalent3, 5.0).build()
        );
        
        EquivalenceResult<Item> itemResult = resultBuilder.resultFor(target, scores);
        
        DBObject dbo = translator.toDBObject(itemResult);
        RestoredEquivalenceResult restoredResult = translator.fromDBObject(dbo);
        
        assertEquals(target.getCanonicalUri(), restoredResult.id());
        assertEquals(target.getTitle(), restoredResult.title());
        
        assertFalse(restoredResult.sourceResults().isEmpty());
        
        EquivalenceIdentifier id1 = new EquivalenceIdentifier(equivalent1.getCanonicalUri(), equivalent1.getTitle(), false, equivalent1.getPublisher().title());
        assertEquals(10.0, restoredResult.sourceResults().get(id1, "source1"));
        assertEquals(5.0, restoredResult.sourceResults().get(id1, "source2"));
        assertNull(restoredResult.sourceResults().get(id1, "source3"));

        EquivalenceIdentifier id2 = new EquivalenceIdentifier(equivalent2.getCanonicalUri(), equivalent2.getTitle(), false, equivalent2.getPublisher().title());
        assertEquals(5.0, restoredResult.sourceResults().get(id2, "source1"));
        assertNull(restoredResult.sourceResults().get(id2, "source2"));
        assertNull(restoredResult.sourceResults().get(id2, "source3"));

        EquivalenceIdentifier id3 = new EquivalenceIdentifier(equivalent3.getCanonicalUri(), equivalent3.getTitle(), false, equivalent3.getPublisher().title());
        assertEquals(5.0, restoredResult.sourceResults().get(id3, "source1"));
        assertEquals(5.0, restoredResult.sourceResults().get(id3, "source2"));
        assertEquals(5.0, restoredResult.sourceResults().get(id3, "source3"));
        
        assertEquals(3, restoredResult.combinedResults().size());
        assertEquals(restoredResult.combinedResults(), ImmutableMap.of(
                id1, 15.0,
                id2, 5.0, 
                id3, 15.0
        ));
    }
}
