package org.atlasapi.equiv.results.persistence;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Iterables.transform;
import static com.metabroadcast.common.persistence.mongo.MongoConstants.ID;
import static org.atlasapi.media.entity.Identified.TO_URI;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.equiv.results.ScoredEquivalent;
import org.atlasapi.equiv.results.ScoredEquivalents;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
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
    private static final String SOURCE_SCORES = "sourceScores";
    private static final String PUBLISHER_SCORES = "publisherScores";
    private static final String SCORES = "scores";
    private static final String COMBINED = "combined";
    private static final String STRONG = "strong";
    private static final String TITLE = "title";


    public <T extends Content> DBObject toDBObject(EquivalenceResult<T> result) {
        DBObject dbo = new BasicDBObject();
        
        TranslatorUtils.from(dbo, ID, result.target().getCanonicalUri());
        TranslatorUtils.from(dbo, TITLE, result.target().getTitle());
        
        TranslatorUtils.fromSet(dbo, copyOf(transform(transform(result.strongEquivalences().values(), ScoredEquivalent.<T>toEquivalent()), TO_URI)), STRONG);
        
        Map<Publisher, List<ScoredEquivalent<T>>> combinedEquivalences = result.combinedEquivalences();
        dbo.put(COMBINED, serializeBinnedEquivalences(combinedEquivalences));

        BasicDBList equivScores = new BasicDBList();
        for (ScoredEquivalents<T> scoredEquivalents : result.rawScores()) {
            BasicDBList sourceScores = serializeBinnedEquivalences(scoredEquivalents.getOrderedEquivalents());
            equivScores.add(new BasicDBObject(ImmutableMap.of(SOURCE, scoredEquivalents.source(), PUBLISHER_SCORES, sourceScores)));
        }
        dbo.put(SOURCE_SCORES, equivScores);
        
        return dbo;
    }

    private <T extends Content> BasicDBList serializeBinnedEquivalences(Map<Publisher, List<ScoredEquivalent<T>>> binnedEquivalences) {
        BasicDBList sourceScores = new BasicDBList();
        for (Entry<Publisher, List<ScoredEquivalent<T>>> scoreBin : binnedEquivalences.entrySet()) {
            sourceScores.add(serializePublisherBin(scoreBin.getKey(), scoreBin.getValue()));
        }
        return sourceScores;
    }

    private <T extends Content> DBObject serializePublisherBin(Publisher key, List<ScoredEquivalent<T>> value) {
        DBObject publisherStrong = new BasicDBObject();
        publisherStrong.put(PUBLISHER, key.key());
        
        BasicDBList publisherEquivalents = new BasicDBList();
        for (ScoredEquivalent<? extends Content> scoredEquivalent : value) {
            publisherEquivalents.add(new BasicDBObject(ImmutableMap.of(
                    ID, scoredEquivalent.equivalent().getCanonicalUri(),
                    TITLE, scoredEquivalent.equivalent().getTitle(),
                    SCORE, scoredEquivalent.score()
            )));
        }
        publisherStrong.put(SCORES, publisherEquivalents);
        return publisherStrong;
    }

    public RestoredEquivalenceResult fromDBObject(DBObject dbo) {
        if(dbo == null) {
            return null;
        }
        
        String targetId = TranslatorUtils.toString(dbo, ID);
        String targetTitle = TranslatorUtils.toString(dbo, TITLE);
        
        Set<String> strongs = TranslatorUtils.toSet(dbo, STRONG);
        
        Map<EquivalenceIdentifier, Double> totals = Maps.newHashMap();
        for (DBObject publisherBin : TranslatorUtils.toDBObjectList(dbo, COMBINED)) {
            String publisher = publisherName(publisherBin);
            for (DBObject strongScore : TranslatorUtils.toDBObjectList(publisherBin, SCORES)) {
                totals.put(identifierFrom(strongScore, publisher,strongs), TranslatorUtils.toDouble(strongScore, SCORE));
            }
        }
        
        Table<String, String, Double> results = HashBasedTable.create();
        for (DBObject sourceEquivScores : TranslatorUtils.toDBObjectList(dbo, SOURCE_SCORES)) {
            String sourceName = TranslatorUtils.toString(sourceEquivScores, SOURCE);
            
            for (DBObject sourceScores : TranslatorUtils.toDBObjectList(sourceEquivScores, PUBLISHER_SCORES)) {
                unserializePublisherBin(results, sourceScores, sourceName);
            }
        }
        
        return new RestoredEquivalenceResult(targetId, targetTitle, results, totals);
    }

    private EquivalenceIdentifier identifierFrom(DBObject strongScore, String publisher, Set<String> strongIds) {
        String id = TranslatorUtils.toString(strongScore, ID);
        String title = TranslatorUtils.toString(strongScore, TITLE);
        EquivalenceIdentifier identifier = new EquivalenceIdentifier(id, title, strongIds.contains(id), publisher);
        return identifier;
    }

    private void unserializePublisherBin(Table<String, String, Double> results, DBObject scoreBin, String sourceName) {
        for (DBObject scoreDbo : TranslatorUtils.toDBObjectList(scoreBin, SCORES)) {
            results.put(TranslatorUtils.toString(scoreDbo, ID), sourceName, TranslatorUtils.toDouble(scoreDbo, SCORE));
        }
    }

    private String publisherName(DBObject scoreBin) {
        Maybe<Publisher> restoredPublisher = Publisher.fromKey(TranslatorUtils.toString(scoreBin, PUBLISHER));
        String publisher = restoredPublisher.hasValue() ? restoredPublisher.requireValue().title() : "Unknown Publisher";
        return publisher;
    }
    
}
