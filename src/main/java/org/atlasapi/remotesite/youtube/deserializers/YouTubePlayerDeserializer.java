package org.atlasapi.remotesite.youtube.deserializers;

import java.lang.reflect.Type;

import org.atlasapi.remotesite.youtube.entity.YouTubePlayer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class YouTubePlayerDeserializer implements JsonDeserializer<YouTubePlayer> {
    @Override
    public YouTubePlayer deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        YouTubePlayer player = new YouTubePlayer();
        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.has("default")) {
            player.setDefaultUrl(jsonObject.get("default").getAsString());
        }
        return player;
    }
}
