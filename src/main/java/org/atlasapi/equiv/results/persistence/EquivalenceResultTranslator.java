package org.atlasapi.equiv.results.persistence;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Iterables.transform;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;
import static org.atlasapi.media.entity.Identified.TO_URI;

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.scores.Score;
import org.atlasapi.equiv.results.scores.ScoredCandidate;
import org.atlasapi.equiv.results.scores.ScoredCandidates;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Publisher;
import org.joda.time.DateTime;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.collect.Table;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.persistence.translator.TranslatorUtils;
import com.metabroadcast.common.time.DateTimeZones;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class EquivalenceResultTranslator {

    private static final String TIMESTAMP = "timestamp";
    private static final String TITLE = "title";
    private static final String EQUIVS = "equivs";
    private static final String STRONG = "strong";

    private static final String COMBINED = "combined";
    private static final String PUBLISHER = "publisher";
    private static final String SCORES = "scores";
    private static final String SOURCE = "source";
    private static final String SCORE = "score";


    
    public <T extends Content> DBObject toDBObject(EquivalenceResult<T> result) {
        DBObject dbo = new BasicDBObject();
        
        final Ordering<Entry<T, Score>> equivalenceResultOrdering = Ordering.from(new Comparator<Entry<T, Score>>() {
            @Override
            public int compare(Entry<T, Score> o1, Entry<T, Score> o2) {
                return o1.getKey().getPublisher().compareTo(o2.getKey().getPublisher());
            }
        }).compound(new Comparator<Entry<T, Score>>() {
            @Override
            public int compare(Entry<T, Score> o1, Entry<T, Score> o2) {
                return Score.SCORE_ORDERING.reverse().compare(o1.getValue(), o2.getValue());
            }
        }).compound(new Comparator<Entry<T, Score>>() {
            @Override
            public int compare(Entry<T, Score> o1, Entry<T, Score> o2) {
                return o1.getKey().getCanonicalUri().compareTo(o2.getKey().getCanonicalUri());
            }
        });
        
        T target = result.subject();
        TranslatorUtils.from(dbo, ID, target.getCanonicalUri());
        TranslatorUtils.from(dbo, TITLE, target.getTitle());
        
        TranslatorUtils.fromSet(dbo, copyOf(transform(transform(result.strongEquivalences().values(), ScoredCandidate.<T>toCandidate()), TO_URI)), STRONG);
        
        BasicDBList equivList = new BasicDBList();
        
        for (Entry<T, Score> combinedEquiv : equivalenceResultOrdering.sortedCopy(result.combinedEquivalences().candidates().entrySet())) {
            DBObject equivDbo = new BasicDBObject();

            T content = combinedEquiv.getKey();
            
            TranslatorUtils.from(equivDbo, ID, content.getCanonicalUri());
            TranslatorUtils.from(equivDbo, TITLE, content.getTitle());
            TranslatorUtils.from(equivDbo, PUBLISHER, content.getPublisher().key());
            TranslatorUtils.from(equivDbo, COMBINED, combinedEquiv.getValue().isRealScore() ? combinedEquiv.getValue().asDouble() : null);
                
            BasicDBList scoreList = new BasicDBList();
            for (ScoredCandidates<T> source : result.rawScores()) {
                Score sourceScore = source.candidates().get(content);
                
                BasicDBObject scoreDbo = new BasicDBObject();
                scoreDbo.put(SOURCE, source.source());
                scoreDbo.put(SCORE, sourceScore != null && sourceScore.isRealScore() ? sourceScore.asDouble() : null);
                
                scoreList.add(scoreDbo);
            }
            TranslatorUtils.from(equivDbo, SCORES, scoreList);
            
            equivList.add(equivDbo);
        }
        TranslatorUtils.from(dbo, EQUIVS, equivList);
        
        TranslatorUtils.fromDateTime(dbo, TIMESTAMP, new DateTime(DateTimeZones.UTC));
        
        dbo.put("desc", result.description().parts());
        
        return dbo;
    }

    public StoredEquivalenceResult fromDBObject(DBObject dbo) {
        if(dbo == null) {
            return null;
        }
        
        String targetId = TranslatorUtils.toString(dbo, ID);
        String targetTitle = TranslatorUtils.toString(dbo, TITLE);
        
        Set<String> strongs = TranslatorUtils.toSet(dbo, STRONG);
        
        ImmutableList.Builder<CombinedEquivalenceScore> totals = ImmutableList.builder();
        Table<Id, String, Double> results = HashBasedTable.create();
        
        for (DBObject equivDbo : TranslatorUtils.toDBObjectList(dbo, EQUIVS)) {
            Id id = Id.valueOf(TranslatorUtils.toLong(equivDbo, ID));
            Double combined = equivDbo.containsField(COMBINED) ? TranslatorUtils.toDouble(equivDbo, COMBINED) : Double.NaN;
            totals.add(
                    new CombinedEquivalenceScore(id, TranslatorUtils.toString(equivDbo, TITLE), combined, strongs.contains(id), publisherName(equivDbo))
            );
            for (DBObject scoreDbo : TranslatorUtils.toDBObjectList(equivDbo, SCORES)) {
                Double score = TranslatorUtils.toDouble(scoreDbo, SCORE);
                results.put(id, TranslatorUtils.toString(scoreDbo, SOURCE), score == null ? Double.NaN : score);
            }
        }
        
        @SuppressWarnings("unchecked") List<Object> description = (List<Object>)dbo.get("desc");
        return new StoredEquivalenceResult(targetId, targetTitle, results, totals.build(), TranslatorUtils.toDateTime(dbo, TIMESTAMP), description);
    }
    
    private String publisherName(DBObject equivDbo) {
        Maybe<Publisher> restoredPublisher = Publisher.fromKey(TranslatorUtils.toString(equivDbo, PUBLISHER));
        String publisher = restoredPublisher.hasValue() ? restoredPublisher.requireValue().title() : "Unknown Publisher";
        return publisher;
    }
    
}
