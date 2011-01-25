package org.atlasapi.remotesite.bbc.ion;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
import org.joda.time.DateTime;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class BbcIonScheduleDeserialiser {

    private Gson gson;

    public BbcIonScheduleDeserialiser() {
        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(DateTime.class, new DateTimeDeserializer())
            .registerTypeAdapter(Integer.class, new IntegerDeserializer())
            .registerTypeAdapter(Boolean.class, new BooleanDeserializer())
            .registerTypeAdapter(URL.class, new URLDeserializer())
            .create();
    }

    public static IonSchedule deserialise(String ionScheduleString) {
        return new BbcIonScheduleDeserialiser().deserialise(ionScheduleString, IonSchedule.class);
    }

    public <T> T deserialise(String ionScheduleString, Class<T> cls) {
        return gson.fromJson(ionScheduleString, cls);
    }
    
    private class DateTimeDeserializer implements JsonDeserializer<DateTime> {
        @Override
        public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String jsonString = json.getAsJsonPrimitive().getAsString();
            if(Strings.isNullOrEmpty(jsonString)) {
                return null;
            }
            return new DateTime(jsonString);
        }
    }

    private class IntegerDeserializer implements JsonDeserializer<Integer> {
        @Override
        public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String jsonString = json.getAsJsonPrimitive().getAsString();
            if(Strings.isNullOrEmpty(jsonString)) {
                return null;
            }
            return json.getAsInt();
        }
    }
    
    public class BooleanDeserializer implements JsonDeserializer<Boolean> {
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
    
    private class URLDeserializer implements JsonDeserializer<URL> {
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
