package org.atlasapi.equiv.results.persistence;

import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;

import java.util.List;
import java.util.Map.Entry;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.ScoredEquivalent;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Objects;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
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


    public DBObject toDBObject(EquivalenceResult<Content> result) {
        DBObject dbo = new BasicDBObject();
        
        TranslatorUtils.from(dbo, ID, result.target().getCanonicalUri());
        TranslatorUtils.from(dbo, TITLE, result.target().getTitle());
        
        BasicDBList strongEquivs = new BasicDBList();
        for (Entry<Publisher, List<ScoredEquivalent<Content>>> strongBin : result.strongEquivalences().entrySet()) {
            strongEquivs.add(serializePublisherBin(strongBin.getKey(), strongBin.getValue()));
        }
        dbo.put(STRONG, strongEquivs);
        
        BasicDBList equivScores = new BasicDBList();
        for (ScoredEquivalents<Content> scoredEquivalents : result.scores()) {
            
            BasicDBList sourceScores = new BasicDBList();
            for (Entry<Publisher, List<ScoredEquivalent<Content>>> scoreBin : scoredEquivalents.getOrderedEquivalents().entrySet()) {
                sourceScores.add(serializePublisherBin(scoreBin.getKey(), scoreBin.getValue()));
            }
            
            equivScores.add(new BasicDBObject(ImmutableMap.of(SOURCE, scoredEquivalents.source(), SCORES, sourceScores)));
        }
        dbo.put(SCORES, equivScores);
        
        return dbo;
    }

    private DBObject serializePublisherBin(Publisher key, List<ScoredEquivalent<Content>> value) {
        DBObject publisherStrong = new BasicDBObject();
        publisherStrong.put(PUBLISHER, key.key());
        
        BasicDBList publisherStrongEquivalents = new BasicDBList();
        for (ScoredEquivalent<Content> scoredEquivalent : value) {
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
        
        Table<EquivalenceIdentifier, String, Double> results = HashBasedTable.create();

        for (DBObject strongBin : TranslatorUtils.toDBObjectList(dbo, STRONG)) {
            unserializePublisherBin(results, strongBin, true, "strong");
            
        }
        
        for (DBObject sourceEquivScores : TranslatorUtils.toDBObjectList(dbo, SCORES)) {
            
            String sourceName = TranslatorUtils.toString(sourceEquivScores, SOURCE);
            
            for (DBObject sourceScores : TranslatorUtils.toDBObjectList(sourceEquivScores, SCORES)) {
                
                unserializePublisherBin(results, sourceScores, false, sourceName);
                
            }
            
        }
        
        
        return new RestoredEquivalenceResult(targetId, targetTitle, results);
    }

    private void unserializePublisherBin(Table<EquivalenceIdentifier, String, Double> results, DBObject scoreBin, boolean strong, String column) {
        Maybe<Publisher> restoredPublisher = Publisher.fromKey(TranslatorUtils.toString(scoreBin, PUBLISHER));
        String publisher = restoredPublisher.hasValue() ? restoredPublisher.requireValue().title() : "Unknown Publisher";
        
        for (DBObject strongScore : TranslatorUtils.toDBObjectList(scoreBin, SCORES)) {
            String id = TranslatorUtils.toString(strongScore, ID);
            String title = TranslatorUtils.toString(strongScore, TITLE);
            Double score = TranslatorUtils.toDouble(strongScore, SCORE);
            results.put(new EquivalenceIdentifier(id, title, strong, publisher), column, score);
        }
    }
    

    public static class EquivalenceIdentifier {

        private final String id;
        private final String title;
        private final boolean strong;
        private final String publisher;

        public EquivalenceIdentifier(String id, String title, boolean strong, String publisher) {
            this.id = id;
            this.title = title;
            this.strong = strong;
            this.publisher = publisher;
        }

        public String id() {
            return id;
        }

        public String title() {
            return title;
        }

        public boolean strong() {
            return strong;
        }

        public String publisher() {
            return publisher;
        }   
        
        @Override
        public int hashCode() {
            return Objects.hashCode(id, title, publisher);
        }
        
        @Override
        public boolean equals(Object that) {
            if(this == that) {
                return true;
            }
            if(that instanceof EquivalenceIdentifier) {
                EquivalenceIdentifier other = (EquivalenceIdentifier) that;
                return id.equals(other.id) && title.equals(other.title) && publisher.equals(other.publisher);
            }
            return false;
        }
    }
}
