package org.atlasapi;

import java.util.Iterator;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;


public class MongoSecondaryReadPreferenceBuilder {

    private static final Splitter TAG_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();
    private static final Splitter KEY_VALUE_SPLITTER = Splitter.on(":").omitEmptyStrings().trimResults(); 
    
    /**
     * Convert from a collection of strings, of format key1:value1,key2:value2
     * into a {@link DBObject} suitable for passing as a paramater to a
     * {@link ReadPreference} instantiation.
     * 
     * @param properties    Strings from which to parse tags
     * @return              A {@link ReadPreference} reflecting tag preferences
     */
    public ReadPreference fromProperties(Iterable<String> properties) {
 
        Iterable<DBObject> tagPreferences = Iterables.transform(properties, PROPERTY_TO_TAG_DBO);
        
        if (Iterables.isEmpty(tagPreferences)) {
            return ReadPreference.secondaryPreferred();
        }
        
        DBObject firstTagPreference = Iterables.get(tagPreferences, 0);
        
        if (Iterables.size(tagPreferences) >= 1) {
            tagPreferences = Iterables.skip(tagPreferences, 1);
        }
        
        return ReadPreference.secondaryPreferred(firstTagPreference, Iterables.toArray(tagPreferences, DBObject.class));
    }
    
    private static Function<String, DBObject> PROPERTY_TO_TAG_DBO = new Function<String, DBObject>() {

        @Override
        public DBObject apply(String input) {
            BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
            for (String tag : TAG_SPLITTER.split(input)) {
                Iterator<String> split = KEY_VALUE_SPLITTER.split(tag).iterator();

                builder.add(split.next(), split.next());
                if (split.hasNext()) {
                    throw new IllegalArgumentException("Invalid format for tag preference; should be key:value");
                }
            }
            return builder.get();
        }
    };
}
