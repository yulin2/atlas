package org.atlasapi.remotesite.youtube.deserializers;

import java.lang.reflect.Type;

import org.atlasapi.remotesite.youtube.entity.YouTubeContent;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class YouTubeContentDeserializer implements JsonDeserializer<YouTubeContent> {
    @Override
    public YouTubeContent deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        YouTubeContent content = new YouTubeContent();
        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.has("1")) {
            content.setOne(jsonObject.get("1").getAsString());
        }
        if (jsonObject.has("5")) {
            content.setFive(jsonObject.get("5").getAsString());
        }
        if (jsonObject.has("6")) {
            content.setSix(jsonObject.get("6").getAsString());
        }
        return content;
    }
}
