package org.atlasapi.remotesite.bbc.ion;

import java.lang.reflect.Type;

import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
import org.joda.time.DateTime;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class BbcIonScheduleDeserialiser {

    private Gson gson;

    public BbcIonScheduleDeserialiser() {
        this.gson = new GsonBuilder()
            .registerTypeAdapter(DateTime.class, new DateTimeDeserializer())
            .registerTypeAdapter(Integer.class, new IntegerDeserializer())
            .create();
    }

    public static IonSchedule deserialise(String ionScheduleString) {
        return new BbcIonScheduleDeserialiser().scheduleFrom(ionScheduleString);
    }

    public IonSchedule scheduleFrom(String ionScheduleString) {
        return gson.fromJson(ionScheduleString, IonSchedule.class);
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

    public class IntegerDeserializer implements JsonDeserializer<Integer> {
        @Override
        public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String jsonString = json.getAsJsonPrimitive().getAsString();
            if(Strings.isNullOrEmpty(jsonString)) {
                return null;
            }
            return json.getAsInt();
        }
    }
}
