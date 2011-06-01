package org.atlasapi.equiv.results.persistence;

import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.EquivalenceResultBuilder;
import org.atlasapi.equiv.results.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.equiv.results.combining.AddingEquivalenceCombiner;
import org.atlasapi.equiv.results.extractors.TopEquivalenceExtractor;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table.Cell;
import com.mongodb.DBObject;

public class EquivalenceResultTranslatorTest extends TestCase {

    private final EquivalenceResultTranslator translator = new EquivalenceResultTranslator();
    private final EquivalenceResultBuilder<Item> resultBuilder = EquivalenceResultBuilder.<Item>resultBuilder(AddingEquivalenceCombiner.<Item>create(), TopEquivalenceExtractor.<Item>create());

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
                DefaultScoredEquivalents.<Item>fromSource("source1").addEquivalent(equivalent1, 5.0).build()
        );
        
        EquivalenceResult<Item> itemResult = resultBuilder.resultFor(target, scores);
        
        DBObject dbo = translator.toDBObject(itemResult);
        RestoredEquivalenceResult restoredResult = translator.fromDBObject(dbo);
        
        assertEquals(target.getCanonicalUri(), restoredResult.id());
        assertEquals(target.getTitle(), restoredResult.title());
        
        assertFalse(restoredResult.sourceResults().isEmpty());
        
        Set<Cell<String, String, Double>> cells = restoredResult.sourceResults().cellSet();
        assertEquals(1, cells.size());
        assertEquals(5.0, Iterables.getOnlyElement(cells).getValue());
        assertEquals(equivalent1.getCanonicalUri(), Iterables.getOnlyElement(cells).getRowKey());
        assertEquals("source1", Iterables.getOnlyElement(cells).getColumnKey());
        
        assertEquals(1, restoredResult.combinedResults().size());
        assertEquals(equivalent1.getCanonicalUri(), Iterables.getOnlyElement(restoredResult.combinedResults().entrySet()).getKey().id());
        assertEquals(5.0, Iterables.getOnlyElement(restoredResult.combinedResults().entrySet()).getValue());
        assertTrue(Iterables.getOnlyElement(restoredResult.combinedResults().entrySet()).getKey().strong());
        
    }
    
    public void testCodecForSinglePublisherResult() {
        
        List<ScoredEquivalents<Item>> scores = ImmutableList.of(
                DefaultScoredEquivalents.<Item>fromSource("source1").addEquivalent(equivalent1, 5.0).addEquivalent(equivalent2, 5.0).addEquivalent(equivalent1, 5.0).build(),
                DefaultScoredEquivalents.<Item>fromSource("source2").addEquivalent(equivalent1, 5.0).build()
        );
        
        EquivalenceResult<Item> itemResult = resultBuilder.resultFor(target, scores);
        
        DBObject dbo = translator.toDBObject(itemResult);
        RestoredEquivalenceResult restoredResult = translator.fromDBObject(dbo);
        
        assertEquals(target.getCanonicalUri(), restoredResult.id());
        assertEquals(target.getTitle(), restoredResult.title());
        
        assertFalse(restoredResult.sourceResults().isEmpty());
        
        String eq1Uri = equivalent1.getCanonicalUri();
        assertEquals(10.0, restoredResult.sourceResults().get(eq1Uri, "source1"));
        assertEquals(5.0, restoredResult.sourceResults().get(eq1Uri, "source2"));

        String eq2Uri = equivalent2.getCanonicalUri();
        assertEquals(5.0, restoredResult.sourceResults().get(eq2Uri, "source1"));
        assertEquals(Double.NaN, restoredResult.sourceResults().get(eq2Uri, "source2"));
        
        EquivalenceIdentifier id1 = new EquivalenceIdentifier(eq1Uri, equivalent1.getTitle(), true, equivalent1.getPublisher().title());
        EquivalenceIdentifier id2 = new EquivalenceIdentifier(eq2Uri, equivalent2.getTitle(), false, equivalent2.getPublisher().title());
        
        assertEquals(2, restoredResult.combinedResults().size());
        assertEquals(restoredResult.combinedResults(), ImmutableMap.of(
                id1, 15.0, 
                id2, 5.0
        ));
    }
    
    public void testCodecForTwoPublisherResult() {
        
        List<ScoredEquivalents<Item>> scores = ImmutableList.of(
                DefaultScoredEquivalents.<Item>fromSource("source1").addEquivalent(equivalent1, 5.0).addEquivalent(equivalent2, 5.0).addEquivalent(equivalent3, 5.0).addEquivalent(equivalent1, 5.0).build(),
                DefaultScoredEquivalents.<Item>fromSource("source2").addEquivalent(equivalent1, 5.0).addEquivalent(equivalent3, 5.0).build(),
                DefaultScoredEquivalents.<Item>fromSource("source3").addEquivalent(equivalent3, 5.0).build()
        );
        
        EquivalenceResult<Item> itemResult = resultBuilder.resultFor(target, scores);
        
        DBObject dbo = translator.toDBObject(itemResult);
        RestoredEquivalenceResult restoredResult = translator.fromDBObject(dbo);
        
        assertEquals(target.getCanonicalUri(), restoredResult.id());
        assertEquals(target.getTitle(), restoredResult.title());
        
        assertFalse(restoredResult.sourceResults().isEmpty());
        
        String eq1Uri = equivalent1.getCanonicalUri();
        assertEquals(10.0, restoredResult.sourceResults().get(eq1Uri, "source1"));
        assertEquals(5.0, restoredResult.sourceResults().get(eq1Uri, "source2"));
        assertEquals(Double.NaN, restoredResult.sourceResults().get(eq1Uri, "source3"));

        String eq2Uri = equivalent2.getCanonicalUri();
        assertEquals(5.0, restoredResult.sourceResults().get(eq2Uri, "source1"));
        assertEquals(Double.NaN, restoredResult.sourceResults().get(eq2Uri, "source2"));
        assertEquals(Double.NaN, restoredResult.sourceResults().get(eq2Uri, "source3"));

        String eq3Uri = equivalent3.getCanonicalUri();
        assertEquals(5.0, restoredResult.sourceResults().get(eq3Uri, "source1"));
        assertEquals(5.0, restoredResult.sourceResults().get(eq3Uri, "source2"));
        assertEquals(5.0, restoredResult.sourceResults().get(eq3Uri, "source3"));
        
        EquivalenceIdentifier id1 = new EquivalenceIdentifier(eq1Uri, equivalent1.getTitle(), true, equivalent1.getPublisher().title());
        EquivalenceIdentifier id2 = new EquivalenceIdentifier(eq2Uri, equivalent2.getTitle(), false, equivalent2.getPublisher().title());
        EquivalenceIdentifier id3 = new EquivalenceIdentifier(eq3Uri, equivalent3.getTitle(), true, equivalent3.getPublisher().title());
        assertEquals(3, restoredResult.combinedResults().size());
        assertEquals(restoredResult.combinedResults(), ImmutableMap.of(
                id1, 15.0,
                id2, 5.0, 
                id3, 15.0
        ));
    }
}
