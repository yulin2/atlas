package org.atlasapi.equiv.tasks.persistence;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.atlasapi.equiv.tasks.SuggestedEquivalents;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.metabroadcast.common.stats.Count;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class SuggestedEquivalentsTranslator {

    private static final String COUNT = "count";
    private static final String TARGET = "target";
    private static final String COUNTS = "counts";
    private static final String PUBLISHER = "publisher";

    public DBObject toDBObject(SuggestedEquivalents<String> suggestions) {

        BasicDBList dbo = new BasicDBList();

        for (Entry<Publisher, List<Count<String>>> binEntry : suggestions.getBinnedCountedSuggestions().entrySet()) {

            BasicDBObject binDbo = new BasicDBObject(PUBLISHER, binEntry.getKey().key());
            binDbo.put(COUNTS, transformCounts(binEntry.getValue()));
            dbo.add(binDbo);

        }

        return dbo;
    }

    private BasicDBList transformCounts(Iterable<Count<String>> counts) {
        BasicDBList countList = new BasicDBList();

        for (Count<String> count : counts) {
            BasicDBObject countDbo = new BasicDBObject(TARGET, count.getTarget());
            countDbo.put(COUNT, count.getCount());
            countList.add(countDbo);
        }

        return countList;
    }

    public SuggestedEquivalents<String> fromDBObject(DBObject dbo) {

        Map<Publisher, List<Count<String>>> binnedCounts = Maps.newHashMap();

        for (Object binObj : ((BasicDBList) dbo)) {
            DBObject binDbo = (BasicDBObject) binObj;

            List<Count<String>> counts = transformCounts((BasicDBList) binDbo.get(COUNTS));
            
            binnedCounts.put(Publisher.fromKey((String) binDbo.get(PUBLISHER)).requireValue(), counts);
        }

        return new SuggestedEquivalents<String>(binnedCounts);
    }

    private <T> List<Count<T>> transformCounts(BasicDBList basicDBList) {
        return ImmutableList.copyOf(Iterables.transform(basicDBList, new Function<Object, Count<T>>() {
            @SuppressWarnings("unchecked")
            @Override
            public Count<T> apply(Object input) {
                DBObject dbo = (DBObject) input;
                return new Count<T>((T)dbo.get(TARGET), (Long) dbo.get(COUNT));
            }
        }));
    }
}
