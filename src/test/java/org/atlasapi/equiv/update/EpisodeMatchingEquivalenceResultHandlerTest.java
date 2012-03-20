package org.atlasapi.equiv.update;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.atlasapi.equiv.handlers.EpisodeMatchingEquivalenceResultHandler;
import org.atlasapi.equiv.handlers.EquivalenceResultHandler;
import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.description.DefaultDescription;
import org.atlasapi.equiv.results.scores.DefaultScoredEquivalents;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredEquivalent;
import org.atlasapi.equiv.results.scores.ScoredEquivalents;
import org.atlasapi.media.content.Episode;
import org.atlasapi.media.content.Item;
import org.atlasapi.media.content.Publisher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class EpisodeMatchingEquivalenceResultHandlerTest extends TestCase {

    //WDYTYA test-case
    public void testStrongerMatchFromStrongBrandIsNotOverWritten() {
        
        Episode target = new Episode("episode","episodeCurie", Publisher.PA);
        target.setSeriesNumber(5);
        target.setEpisodeNumber(5);
        
        Episode strongEpisode = new Episode("strongEp", "strongEpCurie", Publisher.BBC);
        strongEpisode.setEpisodeNumber(5);
        strongEpisode.setSeriesNumber(4);
        
        Episode weakEpisode = new Episode("weakEp", "weakEpCurie", Publisher.BBC);
        weakEpisode.setEpisodeNumber(5);
        weakEpisode.setSeriesNumber(5);
        
        Score score = Score.valueOf(1.0);
        
        List<ScoredEquivalents<Item>> scores = ImmutableList.of();
        ScoredEquivalents<Item> combined = DefaultScoredEquivalents.fromMappedEquivs("test", ImmutableMap.<Item, Score>of());
        
        Map<Publisher, ScoredEquivalent<Item>> strong = ImmutableMap.of(
                Publisher.BBC, ScoredEquivalent.<Item>equivalentScore(strongEpisode, score)
        );
        
        EquivalenceResult<Item> result = new EquivalenceResult<Item>(target, scores , combined , strong, new DefaultDescription());
        
        EquivalenceResultHandler<Item> delegate = new EquivalenceResultHandler<Item>() {
            @Override
            public void handle(EquivalenceResult<Item> result) {
                assertTrue(result.strongEquivalences().get(Publisher.BBC)!= null);
                assertEquals("strongEp", result.strongEquivalences().get(Publisher.BBC).equivalent().getCanonicalUri());
                assertEquals(1.0, result.strongEquivalences().get(Publisher.BBC).score().asDouble());
            }
        };
        
        List<List<Episode>> strongChildren = ImmutableList.<List<Episode>>of(
                ImmutableList.of(strongEpisode, weakEpisode)
        );
        
        EquivalenceResultHandler<Item> handler = new EpisodeMatchingEquivalenceResultHandler(delegate, strongChildren);
        
        handler.handle(result);
        
    }

    public void testEpisodeMatching() {
        
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
        
        Score score = Score.valueOf(1.0);
        
        List<ScoredEquivalents<Item>> scores = ImmutableList.of();
        ScoredEquivalents<Item> combined = DefaultScoredEquivalents.fromMappedEquivs("test", ImmutableMap.<Item, Score>of());
        
        Map<Publisher, ScoredEquivalent<Item>> strong = ImmutableMap.of(
                Publisher.C4, ScoredEquivalent.<Item>equivalentScore(goodEquivalent, score)
        );
        
        EquivalenceResult<Item> result = new EquivalenceResult<Item>(target, scores , combined , strong, new DefaultDescription());
        
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
        
        EquivalenceResultHandler<Item> handler = new EpisodeMatchingEquivalenceResultHandler(delegate, strongChildren);
        
        handler.handle(result);
        
    }
    
}
