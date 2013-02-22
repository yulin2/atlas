package org.atlasapi.remotesite.youtube.entity;

import java.util.List;

public class YouTubeVideoFeed {

    public List<YouTubeVideoEntry> items;

    public int itemsPerPage;
    public int startIndex;
    public int totalItems;

    public List<YouTubeVideoEntry> getVideos() {
        return items;
    }

    public void setVideos(List<YouTubeVideoEntry> items) {
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
