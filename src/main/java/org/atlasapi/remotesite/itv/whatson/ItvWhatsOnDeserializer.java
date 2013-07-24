package org.atlasapi.remotesite.itv.whatson;

import java.io.Reader;
import java.lang.reflect.Type;

import javax.annotation.Nullable;

import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.metabroadcast.common.time.DateTimeZones;


public class ItvWhatsOnDeserializer {
    private static class DateTimeDeserializer implements JsonDeserializer<DateTime> {
        @Override
        public DateTime deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            String value = json.getAsJsonPrimitive().getAsString().replace("/Date(", "").replace(")/", "");
            return new DateTime(DateTimeZones.UTC).withMillis(Long.parseLong(value));
        }}

    private final static Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .registerTypeAdapter(DateTime.class, new DateTimeDeserializer())
            .create();
    
    private static Function<JsonElement, ItvWhatsOnEntry> ENTRY_TRANSLATOR = new Function<JsonElement, ItvWhatsOnEntry>() {
        @Override
        public ItvWhatsOnEntry apply(@Nullable JsonElement input) {
            return gson.fromJson(input, ItvWhatsOnEntry.class);
        }};
    
    public ItvWhatsOnDeserializer() {
       
    }
    
    public FluentIterable<ItvWhatsOnEntry> deserialize(String json) {
        JsonParser parser = new JsonParser();
        JsonArray entries = parser.parse(json).getAsJsonArray();
        return FluentIterable.from(entries).transform(ENTRY_TRANSLATOR);
    }
    
    public FluentIterable<ItvWhatsOnEntry> deserialize(Reader json) {
        JsonParser parser = new JsonParser();
        JsonArray entries = parser.parse(json).getAsJsonArray();
        return FluentIterable.from(entries).transform(ENTRY_TRANSLATOR);
    }

}
