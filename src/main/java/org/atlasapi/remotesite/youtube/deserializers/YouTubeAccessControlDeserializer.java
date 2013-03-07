package org.atlasapi.remotesite.youtube.deserializers;

import java.lang.reflect.Type;

import org.atlasapi.remotesite.youtube.entity.YouTubeAccessControl;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class YouTubeAccessControlDeserializer implements
        JsonDeserializer<YouTubeAccessControl> {
    @Override
    public YouTubeAccessControl deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        YouTubeAccessControl accessControl = new YouTubeAccessControl();
        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.has("autoPlay")) {
            accessControl.setAutoPlay(getPermission(jsonObject.get("autoPlay")
                    .getAsString()));
        }
        if (jsonObject.has("comment")) {
            accessControl.setAutoPlay(getPermission(jsonObject.get("comment")
                    .getAsString()));
        }
        if (jsonObject.has("commentVote")) {
            accessControl.setAutoPlay(getPermission(jsonObject.get(
                    "commentVote").getAsString()));
        }
        if (jsonObject.has("videoRespond")) {
            accessControl.setAutoPlay(getPermission(jsonObject.get(
                    "videoRespond").getAsString()));
        }
        if (jsonObject.has("rate")) {
            accessControl.setAutoPlay(getPermission(jsonObject.get("rate")
                    .getAsString()));
        }
        if (jsonObject.has("embed")) {
            accessControl.setAutoPlay(getPermission(jsonObject.get("embed")
                    .getAsString()));
        }
        if (jsonObject.has("list")) {
            accessControl.setAutoPlay(getPermission(jsonObject.get("list")
                    .getAsString()));
        }
        if (jsonObject.has("syndicate")) {
            accessControl.setAutoPlay(getPermission(jsonObject.get("syndicate")
                    .getAsString()));
        }
        return accessControl;
    }

    public Permission getPermission(String value) {
        if ("moderated".equalsIgnoreCase(value)) {
            return Permission.MODERATED;
        } else if ("denied".equalsIgnoreCase(value)) {
            return Permission.DENIED;
        } else if ("allowed".equalsIgnoreCase(value)) {
            return Permission.ALLOWED;
        }
        return Permission.UNDEFINED;
    }

    /**
     * Allowed : 1<br>
     * Denied : 0<br>
     * Moderated : 2<br>
     * Undefined: -1
     * 
     * @author augusto
     * 
     */
    public enum Permission {
        ALLOWED(1), DENIED(0), MODERATED(2), UNDEFINED(-1);
        private int permission;

        private Permission(int permission) {
            this.permission = permission;
        }

        public int getPermission() {
            return permission;
        }
    }

}
