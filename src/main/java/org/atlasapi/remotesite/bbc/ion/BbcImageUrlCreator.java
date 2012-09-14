package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Series;

class BbcImageUrlCreator {

    static void addImagesTo(String prefix, String pid, Content item) {
        String path = prefix + pid;
        item.setThumbnail(path + "_150_84.jpg");
        item.setImage(path + "_640_360.jpg");
    }
    
    static void addIplayerImagesTo(String pid, Item item) {
        addImagesTo("http://www.bbc.co.uk/iplayer/images/episode/", pid, item);
    }
    
    static void addIplayerImagesTo(String pid, Brand brand) {
        addImagesTo("http://www.bbc.co.uk/iplayer/images/progbrand/", pid, brand);
    }
    
    static void addIplayerImagesTo(String pid, Series series) {
        addImagesTo("http://www.bbc.co.uk/iplayer/images/series/", pid, series);
    }
}
