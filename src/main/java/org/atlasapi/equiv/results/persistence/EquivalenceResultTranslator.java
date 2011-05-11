package org.atlasapi.equiv.results.persistence;

import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.ScoredEquivalent;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class EquivalenceResultTranslator {

    private static final String SOURCE = "source";
    private static final String SCORE = "score";
    private static final String PUBLISHER = "publisher";
    private static final String SCORES = "scores";
    private static final String STRONG = "strong";
    private static final String TITLE = "title";


    public <T extends Content> DBObject toDBObject(EquivalenceResult<T> result) {
        DBObject dbo = new BasicDBObject();
        
        TranslatorUtils.from(dbo, ID, result.target().getCanonicalUri());
        TranslatorUtils.from(dbo, TITLE, result.target().getTitle());
        
        //Set of strong equiv canonical uris.
        TranslatorUtils.fromSet(
            dbo, 
            ImmutableSet.copyOf(Iterables.transform(
                    Iterables.concat(result.strongEquivalences().values()), 
                    new Function<ScoredEquivalent<? extends Content>, String>() {
                        @Override
                        public String apply(ScoredEquivalent<? extends Content> input) {
                            return input.equivalent().getCanonicalUri();
                        }
                    })
            ),
            STRONG
         );

        BasicDBList equivScores = new BasicDBList();
        for (ScoredEquivalents<T> scoredEquivalents : result.scores()) {
            
            BasicDBList sourceScores = new BasicDBList();
            for (Entry<Publisher, List<ScoredEquivalent<T>>> scoreBin : scoredEquivalents.getOrderedEquivalents().entrySet()) {
                sourceScores.add(serializePublisherBin(scoreBin.getKey(), scoreBin.getValue()));
            }
            
            equivScores.add(new BasicDBObject(ImmutableMap.of(SOURCE, scoredEquivalents.source(), SCORES, sourceScores)));
        }
        dbo.put(SCORES, equivScores);
        
        return dbo;
    }

    private <T extends Content> DBObject serializePublisherBin(Publisher key, List<ScoredEquivalent<T>> value) {
        DBObject publisherStrong = new BasicDBObject();
        publisherStrong.put(PUBLISHER, key.key());
        
        BasicDBList publisherStrongEquivalents = new BasicDBList();
        for (ScoredEquivalent<? extends Content> scoredEquivalent : value) {
            publisherStrongEquivalents.add(new BasicDBObject(ImmutableMap.of(
                    ID, scoredEquivalent.equivalent().getCanonicalUri(),
                    TITLE, scoredEquivalent.equivalent().getTitle(),
                    SCORE, scoredEquivalent.score()
            )));
        }
        publisherStrong.put(SCORES, publisherStrongEquivalents);
        return publisherStrong;
    }

    public RestoredEquivalenceResult fromDBObject(DBObject dbo) {
        if(dbo == null) {
            return null;
        }
        
        String targetId = TranslatorUtils.toString(dbo, ID);
        String targetTitle = TranslatorUtils.toString(dbo, TITLE);
        Set<String> strongUris = TranslatorUtils.toSet(dbo, STRONG);
        
        Table<EquivalenceIdentifier, String, Double> results = HashBasedTable.create();
        
        for (DBObject sourceEquivScores : TranslatorUtils.toDBObjectList(dbo, SCORES)) {
            
            String sourceName = TranslatorUtils.toString(sourceEquivScores, SOURCE);
            
            for (DBObject sourceScores : TranslatorUtils.toDBObjectList(sourceEquivScores, SCORES)) {
                unserializePublisherBin(results, sourceScores, sourceName, strongUris);
            }
        }
        
        return new RestoredEquivalenceResult(targetId, targetTitle, results);
    }

    private void unserializePublisherBin(Table<EquivalenceIdentifier, String, Double> results, DBObject scoreBin, String sourceName, Set<String> strongUris) {
        Maybe<Publisher> restoredPublisher = Publisher.fromKey(TranslatorUtils.toString(scoreBin, PUBLISHER));
        String publisher = restoredPublisher.hasValue() ? restoredPublisher.requireValue().title() : "Unknown Publisher";
        
        for (DBObject strongScore : TranslatorUtils.toDBObjectList(scoreBin, SCORES)) {
            String id = TranslatorUtils.toString(strongScore, ID);
            String title = TranslatorUtils.toString(strongScore, TITLE);
            Double score = TranslatorUtils.toDouble(strongScore, SCORE);
            results.put(new EquivalenceIdentifier(id, title, strongUris.contains(id), publisher), sourceName, score);
        }
    }
    
}
