package org.atlasapi.remotesite.getty;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GettyAdapter {

    private static final String SEARCH_RESULT = "SearchForVideosResult";
    private static final String VIDEOS = "Videos";
    
    private static final String ASSET_ID = "AssetId";
    private static final String DESCRIPTION = "Caption";
    private static final String DURATION = "ClipLength";
    private static final String TITLE = "Title";
    private static final String URLS = "Urls";
    private static final String THUMB = "Thumb";
    private static final String DATE_CREATED = "DateCreated";
    private static final String KEYWORDS = "Keywords";
    private static final String KEYWORD_TITLE = "Text";
    private static final String ASPECT_RATIOS = "AspectRatios";
    
    public List<VideoResponse> parse(String text) {
        JsonObject parse = (JsonObject) new JsonParser().parse(text);
        JsonArray videoArray = parse.get(SEARCH_RESULT).getAsJsonObject().get(VIDEOS).getAsJsonArray();
        Builder<VideoResponse> videos = new ImmutableList.Builder<VideoResponse>();
        //check here so that json parser doesn't fail
        if (videoArray.size() != 0) {
            for (JsonElement elem : videoArray) {
                videos.add(createVideoResponse(elem.getAsJsonObject()));
            }
        }
        return videos.build();
    }
    
    private VideoResponse createVideoResponse(JsonObject elem) {
        VideoResponse videoResponse = new VideoResponse();
        
        videoResponse.setAssetId(elem.get(ASSET_ID).getAsString());
        videoResponse.setDescription(elem.get(DESCRIPTION).getAsString());
        videoResponse.setDuration(elem.get(DURATION).getAsString());
        videoResponse.setKeywords(keywords(elem));
        videoResponse.setThumb(thumb(elem));
        videoResponse.setDateCreated(elem.get(DATE_CREATED).getAsString());
        videoResponse.setTitle(elem.get(TITLE).getAsString());
        videoResponse.setAspectRatios(aspectRatios(elem));
        
        return videoResponse;
    }

    private List<String> aspectRatios(JsonObject obj) {
        Builder<String> aspectRatios = new ImmutableList.Builder<String>();
        JsonArray keys = obj.get(ASPECT_RATIOS).getAsJsonArray();
        for (JsonElement elem : keys) {
            aspectRatios.add(elem.getAsString());
        }
        return aspectRatios.build();
    }

    private String thumb(JsonObject elem) {
        return elem.get(URLS).getAsJsonObject().get(THUMB).getAsString();
    }

    private List<String> keywords(JsonObject obj) {
        Builder<String> keywords = new ImmutableList.Builder<String>();
        JsonArray keys = obj.get(KEYWORDS).getAsJsonArray();
        for (JsonElement elem : keys) {
            keywords.add(elem.getAsJsonObject().get(KEYWORD_TITLE).getAsString());
        }
        return keywords.build();
    }
    
}
