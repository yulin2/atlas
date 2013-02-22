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
<<<<<<< HEAD
    private final DateTimeFormatter format;
    
    public LocalDateDeserializer(DateTimeFormatter formatter){
        this.format = formatter;
    }
=======
    DateTimeFormatter format = ISODateTimeFormat.date();
>>>>>>> a715d5a... IN PROGRESS - issue MBST-4533: YouTube adapter 

    public LocalDate deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        return format.parseLocalDate(json.getAsString());
    }
}
