package org.atlasapi.remotesite.getty;

import java.util.List;

import com.google.common.base.Objects;

public class VideoResponse {

    private String assetId;
    private String title;
    private String description;
    private String duration;
    private String thumb;
    private String dateCreated;
    private List<String> keywords;
    
    public String getAssetId() {
        return assetId;
    }
    
    public void setAssetId(String assetId) {
        this.assetId = assetId;
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
    
    public String getDuration() {
        return duration;
    }
    
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    public String getThumb() {
        return thumb;
    }
    
    public void setThumb(String thumb) {
        this.thumb = thumb;
    }
    
    public List<String> getKeywords() {
        return keywords;
    }
    
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
    
    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreted) {
        this.dateCreated = dateCreted;
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("assetId", assetId)
                .add("title", title)
                .add("description", description)
                .add("duration", duration)
                .add("thumb", thumb)
                .add("keywords", keywords)
                .toString();
    }
    
}
