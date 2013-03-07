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
        ALLOWED("allowed"), DENIED("denied"), MODERATED("moderated"), UNDEFINED("undefined");
        private String permission;

        private Permission(String permission) {
            this.permission = permission;
        }

        public String getPermission() {
            return permission;
        }
    }

}
