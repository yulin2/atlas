package org.atlasapi.remotesite.youtube.deserializers;

import java.lang.reflect.Type;

import org.atlasapi.remotesite.youtube.entity.YouTubeThumbnail;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class YouTubeThumbnailDeserializer implements JsonDeserializer<YouTubeThumbnail> {
    @Override
    public YouTubeThumbnail deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        YouTubeThumbnail thumbnail = new YouTubeThumbnail();
        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.has("default")) {
            thumbnail.setDefaultUrl(jsonObject.get("default").getAsString());
        }
        if (jsonObject.has("sqDefault")) {
            thumbnail.setSqDefault(jsonObject.get("sqDefault").getAsString());
        }
        if (jsonObject.has("hqDefault")) {
            thumbnail.setHqDefault(jsonObject.get("hqDefault").getAsString());
        }
        return thumbnail;
    }
}
