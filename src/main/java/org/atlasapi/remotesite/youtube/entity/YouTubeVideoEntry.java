package org.atlasapi.remotesite.youtube.entity;

import org.atlasapi.remotesite.youtube.entity.YouTubeAccessControl;
import org.atlasapi.remotesite.youtube.entity.YouTubeContent;
import org.atlasapi.remotesite.youtube.entity.YouTubePlayer;
import org.atlasapi.remotesite.youtube.entity.YouTubeThumbnail;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * API version 2.1
 */
public class YouTubeVideoEntry {
        String id;
        String title;
        String description;
        String category;
        YouTubePlayer player;
        // Tags were removed from the public metadata -
        // http://productforums.google.com/forum/#!topic/youtube/1bfUDA_iFu8%5B1-25-false%5D
        // List<String> tags = Lists.newArrayList();
        YouTubeThumbnail thumbnail;
        int duration;
        YouTubeContent content;
        // New fields
        String uploader;
        String location;
        String aspectRatio;
        Float rating;
        DateTime uploaded;
        DateTime updated;
        LocalDate recorded;
        int commentCount;
        int viewCount;
        String likeCount;
        int ratingCount;
        int favoriteCount;
        YouTubeAccessControl accessControl;

        public String getUploader() {
            return uploader;
        }

        public void setUploader(String uploader) {
            this.uploader = uploader;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public LocalDate getRecorded() {
            return recorded;
        }

        public void setRecorded(LocalDate recorded) {
            this.recorded = recorded;
        }

        public String getLikeCount() {
            return likeCount;
        }

        public void setLikeCount(String likeCount) {
            this.likeCount = likeCount;
        }

        public int getRatingCount() {
            return ratingCount;
        }

        public void setRatingCount(int ratingCount) {
            this.ratingCount = ratingCount;
        }

        public DateTime getUploaded() {
            return uploaded;
        }

        public void setUploaded(DateTime uploaded) {
            this.uploaded = uploaded;
        }

        public DateTime getUpdated() {
            return updated;
        }

        public void setUpdated(DateTime updated) {
            this.updated = updated;
        }

        public String getAspectRatio() {
            return aspectRatio;
        }

        public void setAspectRatio(String aspectRatio) {
            this.aspectRatio = aspectRatio;
        }

        public Float getRating() {
            return rating;
        }

        public void setRating(Float rating) {
            this.rating = rating;
        }

        public int getViewCount() {
            return viewCount;
        }

        public void setViewCount(int viewCount) {
            this.viewCount = viewCount;
        }

        public int getFavoriteCount() {
            return favoriteCount;
        }

        public void setFavoriteCount(int favoriteCount) {
            this.favoriteCount = favoriteCount;
        }

        public int getCommentCount() {
            return commentCount;
        }

        public void setCommentCount(int commentCount) {
            this.commentCount = commentCount;
        }

        public YouTubeAccessControl getAccessControl() {
            return accessControl;
        }

        public void setAccessControl(YouTubeAccessControl accessControl) {
            this.accessControl = accessControl;
        }

        // End of new fields

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

        public YouTubePlayer getPlayer() {
            return player;
        }

        public void setPlayer(YouTubePlayer player) {
            this.player = player;
        }

        public YouTubeThumbnail getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(YouTubeThumbnail thumbnail) {
            this.thumbnail = thumbnail;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public YouTubeContent getContent() {
            return content;
        }

        public void setContent(YouTubeContent content) {
            this.content = content;
        }
    }
