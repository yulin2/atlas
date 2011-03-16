package org.atlasapi.equiv.tasks.persistence;

import java.util.List;

import org.atlasapi.equiv.tasks.ContainerEquivResult;
import org.atlasapi.equiv.tasks.EquivResult;
import org.atlasapi.equiv.tasks.SuggestedEquivalents;
import org.atlasapi.media.entity.Described;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.persistence.mongo.MongoConstants;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ContainerEquivResultTranslator {

    private static final Function<? super Described, String> TRANSFORMER = new Function<Described, String>() {
        @Override
        public String apply(Described input) {
            return String.format("%s/%s", input.getTitle(), input.getCanonicalUri());
        }
    };

    private static final String SUGGESTED = "suggested";
    private static final String FULL_MATCH = "fullMatch";
    private static final String CERTAINTY = "certainty";
    private static final String SUB = "sub";

    private final SuggestedEquivalentsTranslator suggestionsTranslator;

    public ContainerEquivResultTranslator() {
        this.suggestionsTranslator = new SuggestedEquivalentsTranslator();
    }

    public <T extends Described, U extends Described> DBObject toDBObject(ContainerEquivResult<T, U> result) {
        DBObject dbo = toDBObject((EquivResult<T>)result);

        List<EquivResult<U>> subResults = result.getItemResults();
        if (!subResults.isEmpty()) {
            BasicDBList subList = new BasicDBList();
            subList.addAll(ImmutableList.copyOf(Iterables.transform(subResults, new Function<EquivResult<U>, DBObject>() {
                @Override
                public DBObject apply(EquivResult<U> input) {
                    return toDBObject(input);
                }
            })));
            dbo.put(SUB, subList);
        }

        return dbo;
    }

    public <T extends Described> DBObject toDBObject(EquivResult<T> result) {
        BasicDBObject dbo = new BasicDBObject();

        dbo.put(MongoConstants.ID, TRANSFORMER.apply(result.described()));

        dbo.put(CERTAINTY, result.certainty());
        dbo.put(FULL_MATCH, result.fullMatch());

        SuggestedEquivalents<T> suggestedEquivalents = result.suggestedEquivalents();
        if (suggestedEquivalents != null) {
            dbo.put(SUGGESTED, suggestionsTranslator.toDBObject(suggestedEquivalents.stringResult(TRANSFORMER)));
        }
        
        return dbo;
    }

    public EquivResult<String> fromDBObject(DBObject dbo) {

        String desc = (String) dbo.get(MongoConstants.ID);

        SuggestedEquivalents<String> suggestions = suggestionsTranslator.fromDBObject((DBObject) dbo.get(SUGGESTED));

        if (dbo.containsField(SUB)) {
            Iterable<EquivResult<String>> subResults = Iterables.transform((BasicDBList) dbo.get(SUB), new Function<Object, EquivResult<String>>() {
                @Override
                public EquivResult<String> apply(Object input) {
                    return fromDBObject((DBObject) input);
                }
            });
            return new ContainerEquivResult<String, String>(desc, (Integer) dbo.get(FULL_MATCH), suggestions, (Double) dbo.get(CERTAINTY)).withItemResults(subResults);
        }

        return new EquivResult<String>(desc, (Integer) dbo.get(FULL_MATCH), suggestions, (Double) dbo.get(CERTAINTY));
    }

}
