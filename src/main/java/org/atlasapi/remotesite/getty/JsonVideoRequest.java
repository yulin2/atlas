package org.atlasapi.remotesite.getty;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonVideoRequest {

    public static JsonElement toJson(VideoRequest src) {
        JsonObject json = new JsonObject();
        
        JsonObject requestHeader = new JsonObject();
        requestHeader.addProperty("Token", src.getToken());
        
        JsonObject query = new JsonObject();
        query.addProperty("SearchPhrase", src.getSearchPhrase());
        
        JsonObject resultOptions = new JsonObject();
        resultOptions.addProperty("ItemCount", src.getItemCount());
        resultOptions.addProperty("ItemStartNumber", src.getItemStartNumber());
        
        JsonObject searchForVideosRequestBody = new JsonObject();
        searchForVideosRequestBody.add("Query", query);
        searchForVideosRequestBody.add("ResultOptions", resultOptions);
        
        json.add("RequestHeader", requestHeader);
        json.add("SearchForVideosRequestBody", searchForVideosRequestBody);
        
        return json;
    }

}
