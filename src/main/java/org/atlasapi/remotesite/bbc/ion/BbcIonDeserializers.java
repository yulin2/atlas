package org.atlasapi.remotesite.bbc.ion;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.joda.time.DateTime;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class BbcIonDeserializers {
    public static class DateTimeDeserializer implements JsonDeserializer<DateTime> {
        @Override
        public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String jsonString = json.getAsJsonPrimitive().getAsString();
            if(Strings.isNullOrEmpty(jsonString)) {
                return null;
            }
            return new DateTime(jsonString);
        }
    }

    public static class IntegerDeserializer implements JsonDeserializer<Integer> {
        @Override
        public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String jsonString = json.getAsJsonPrimitive().getAsString();
            if(Strings.isNullOrEmpty(jsonString)) {
                return null;
            }
            return json.getAsInt();
        }
    }
    
    public static class BooleanDeserializer implements JsonDeserializer<Boolean> {
        private Map<String, Boolean> boolMap = ImmutableMap.of(
                "true", Boolean.TRUE,
                "false",Boolean.FALSE,
                "1",    Boolean.TRUE,
                "0",    Boolean.FALSE
        );

        @Override
        public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return boolMap.get(json.getAsJsonPrimitive().getAsString());
        }
    }
    
    public static class URLDeserializer implements JsonDeserializer<URL> {
        @Override
        public URL deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String jsonString = json.getAsJsonPrimitive().getAsString();
            if(Strings.isNullOrEmpty(jsonString)) {
                return null;
            }try{
                return new URL(json.getAsString());
            }catch (MalformedURLException e) {
                throw new JsonParseException(e);
            }
        }
    }
}
