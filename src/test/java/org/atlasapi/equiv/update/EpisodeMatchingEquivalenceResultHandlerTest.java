package org.atlasapi.equiv.update;

import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.EquivalenceResultHandler;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class EpisodeMatchingEquivalenceResultHandlerTest extends TestCase {

    public void testEpisodeMatchingFiltering() {

        Set<Container> strongContainers = ImmutableSet.<Container>of(new Brand("brand", "brandCurie", Publisher.BBC));
        List<List<Episode>> strongChildren = ImmutableList.of();
        
        Episode target = new Episode("episode","episodeCurie", Publisher.PA);
        
        Episode badEquivalent = new Episode("bequiv","bequivCurie",Publisher.C4);
        badEquivalent.setParentRef(new ParentRef("weakBrand"));
        
        Episode goodEquiv = new Episode("gequiv", "gequivCurie", Publisher.BBC);
        goodEquiv.setParentRef(new ParentRef("brand"));
        
        Score score = Score.valueOf(1.0);
        
        List<ScoredEquivalents<Item>> scores = ImmutableList.of();
        ScoredEquivalents<Item> combined = DefaultScoredEquivalents.fromMappedEquivs("test", ImmutableMap.<Item, Score>of());
        
        Map<Publisher, ScoredEquivalent<Item>> strong = ImmutableMap.of(
                Publisher.C4, ScoredEquivalent.<Item>equivalentScore(badEquivalent, score),
                Publisher.BBC,ScoredEquivalent.<Item>equivalentScore(goodEquiv, score)
       );
        
        EquivalenceResult<Item> result = new EquivalenceResult<Item>(target, scores , combined , strong);
        
        EquivalenceResultHandler<Item> delegate = new EquivalenceResultHandler<Item>() {
            @Override
            public void handle(EquivalenceResult<Item> result) {
                assertTrue(result.strongEquivalences().get(Publisher.C4) == null);
                assertTrue(result.strongEquivalences().get(Publisher.BBC) != null);
                assertTrue(result.strongEquivalences().get(Publisher.BBC).equivalent().getCanonicalUri().equals("gequiv"));
            }
        };
        
        EpisodeMatchingEquivalenceResultHandler handler = new EpisodeMatchingEquivalenceResultHandler(delegate, strongContainers, strongChildren);
        
        handler.handle(result);
        
    }

    public void testEpisodeMatching() {
        

        Set<Container> strongContainers = ImmutableSet.<Container>of(
                new Brand("bbc", "bbcCurie", Publisher.BBC),
                new Brand("c4", "c4curie", Publisher.C4)
        );
        
        Episode strongEpisode = new Episode("strongEp", "strongEpCurie", Publisher.BBC);
        strongEpisode.setEpisodeNumber(5);
        strongEpisode.setSeriesNumber(5);
        
        List<List<Episode>> strongChildren = ImmutableList.<List<Episode>>of(
                ImmutableList.of(strongEpisode)
        );
        
        Episode target = new Episode("episode","episodeCurie", Publisher.PA);
        target.setSeriesNumber(5);
        target.setEpisodeNumber(5);
        
        Episode goodEquivalent = new Episode("gequiv","gequivCurie",Publisher.C4);
        goodEquivalent.setParentRef(new ParentRef("c4"));
        
        Score score = Score.valueOf(1.0);
        
        List<ScoredEquivalents<Item>> scores = ImmutableList.of();
        ScoredEquivalents<Item> combined = DefaultScoredEquivalents.fromMappedEquivs("test", ImmutableMap.<Item, Score>of());
        
        Map<Publisher, ScoredEquivalent<Item>> strong = ImmutableMap.of(
                Publisher.C4, ScoredEquivalent.<Item>equivalentScore(goodEquivalent, score)
       );
        
        EquivalenceResult<Item> result = new EquivalenceResult<Item>(target, scores , combined , strong);
        
        EquivalenceResultHandler<Item> delegate = new EquivalenceResultHandler<Item>() {
            @Override
            public void handle(EquivalenceResult<Item> result) {
                assertTrue(result.strongEquivalences().get(Publisher.C4) != null);
                assertTrue(result.strongEquivalences().get(Publisher.C4).equivalent().getCanonicalUri().equals("gequiv"));
                assertTrue(result.strongEquivalences().get(Publisher.BBC)!= null);
                assertTrue(result.strongEquivalences().get(Publisher.BBC).equivalent().getCanonicalUri().equals("strongEp"));
                assertTrue(result.strongEquivalences().get(Publisher.BBC).score().asDouble() == 2.0);
            }
        };
        
        EpisodeMatchingEquivalenceResultHandler handler = new EpisodeMatchingEquivalenceResultHandler(delegate, strongContainers, strongChildren);
        
        handler.handle(result);
        
    }
    
}
