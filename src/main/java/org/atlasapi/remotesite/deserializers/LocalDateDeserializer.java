package org.atlasapi.remotesite.deserializers;

import java.lang.reflect.Type;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class LocalDateDeserializer implements JsonDeserializer<LocalDate> {
    DateTimeFormatter format = ISODateTimeFormat.date();

    public LocalDate deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        return format.parseLocalDate(json.getAsString());
    }
}
