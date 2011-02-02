package org.atlasapi.remotesite.youtube;

import java.lang.reflect.Type;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class YouTubeModel {
    public static class VideoWrapper {
        VideoEntry data;
        public VideoEntry getData() {
            return data;
        }
        public void setData(VideoEntry data) {
            this.data = data;
        }
    }
    public static class FeedWrapper {
        VideoFeed data;
        public VideoFeed getData() {
            return data;
        }
        public void setData(VideoFeed data) {
            this.data = data;
        }
    }
    public static class VideoFeed {
        public List<VideoEntry> items;
        
        public int itemsPerPage;
        public int startIndex;
        public int totalItems;
        public List<VideoEntry> getVideos() {
            return items;
        }
        public void setVideos(List<VideoEntry> items) {
            this.items = items;
        }
        public int getItemsPerPage() {
            return itemsPerPage;
        }
        public void setItemsPerPage(int itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
        }
        public int getStartIndex() {
            return startIndex;
        }
        public void setStartIndex(int startIndex) {
            this.startIndex = startIndex;
        }
        public int getTotalItems() {
            return totalItems;
        }
        public void setTotalItems(int totalItems) {
            this.totalItems = totalItems;
        }
    }

    public static class VideoEntry {
        String id;
        String title;
        String description;
        String category;
        Player player;
        List<String> tags = Lists.newArrayList();
        Thumbnail thumbnail;
        int duration;
        Content content;
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
        public String getCategory() {
            return category;
        }
        public void setCategory(String category) {
            this.category = category;
        }
        public Player getPlayer() {
            return player;
        }
        public void setPlayer(Player player) {
            this.player = player;
        }
        public List<String> getTags() {
            return tags;
        }
        public void setTags(List<String> tags) {
            this.tags = tags;
        }
        public Thumbnail getThumbnail() {
            return thumbnail;
        }
        public void setThumbnail(Thumbnail thumbnail) {
            this.thumbnail = thumbnail;
        }
        public int getDuration() {
            return duration;
        }
        public void setDuration(int duration) {
            this.duration = duration;
        }
        public Content getContent() {
            return content;
        }
        public void setContent(Content content) {
            this.content = content;
        }
    }
    
    public static class Content {
        String one;
        String five;
        String six;
        public String getOne() {
            return one;
        }
        public void setOne(String one) {
            this.one = one;
        }
        public String getFive() {
            return five;
        }
        public void setFive(String five) {
            this.five = five;
        }
        public String getSix() {
            return six;
        }
        public void setSix(String six) {
            this.six = six;
        }
    }
    
    public static class ContentDeserializer implements JsonDeserializer<Content> {
        @Override
        public Content deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Content content = new Content();
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
    
    public static class Player {
        String defaultUrl;

        public String getDefaultUrl() {
            return defaultUrl;
        }

        public void setDefaultUrl(String defaultUrl) {
            this.defaultUrl = defaultUrl;
        }
    }
    
    public static class PlayerDeserializer implements JsonDeserializer<Player> {
        @Override
        public Player deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Player player = new Player();
            JsonObject jsonObject = json.getAsJsonObject();
            if (jsonObject.has("default")) {
                player.setDefaultUrl(jsonObject.get("default").getAsString());
            }
            return player;
        }
    }
    
    public static class Thumbnail {
        String defaultUrl;
        String sqDefault;
        String hqDefault;
        public String getDefaultUrl() {
            return defaultUrl;
        }
        public void setDefaultUrl(String defaultUrl) {
            this.defaultUrl = defaultUrl;
        }
        public String getSqDefault() {
            return sqDefault;
        }
        public void setSqDefault(String sqDefault) {
            this.sqDefault = sqDefault;
        }
        public String getHqDefault() {
            return hqDefault;
        }
        public void setHqDefault(String hqDefault) {
            this.hqDefault = hqDefault;
        }
    }
    
    public static class ThumbnailDeserializer implements JsonDeserializer<Thumbnail> {
        @Override
        public Thumbnail deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Thumbnail thumbnail = new Thumbnail();
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
}
