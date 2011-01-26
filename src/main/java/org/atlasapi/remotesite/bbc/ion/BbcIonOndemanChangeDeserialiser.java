package org.atlasapi.remotesite.bbc.ion;

import java.net.URL;

import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.BooleanDeserializer;
import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.DateTimeDeserializer;
import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.IntegerDeserializer;
import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.URLDeserializer;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChanges;
import org.joda.time.DateTime;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BbcIonOndemanChangeDeserialiser {

    private final Gson gson;

    public BbcIonOndemanChangeDeserialiser() {
        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(DateTime.class, new DateTimeDeserializer())
            .registerTypeAdapter(Integer.class, new IntegerDeserializer())
            .registerTypeAdapter(Boolean.class, new BooleanDeserializer())
            .registerTypeAdapter(URL.class, new URLDeserializer())
            .create();
    }

    public IonOndemandChanges deserialise(String ionString) {
        return gson.fromJson(ionString, IonOndemandChanges.class);
    }
}
