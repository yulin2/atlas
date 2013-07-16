package org.atlasapi.remotesite.talktalk;

import org.atlasapi.media.entity.Image;
import org.atlasapi.remotesite.talktalk.vod.bindings.ImageListType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ImageType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;

import com.google.common.collect.ImmutableSet;

/**
 * Extracts {@link Image}s from TalkTalk {@link ItemDetailType} according to <a
 * href="http://docs.metabroadcast.com/display/mbst/TalkTalk+VOD">http://docs.
 * metabroadcast.com/display/mbst/TalkTalk+VOD</a>
 * 
 */
public class TalkTalkImagesExtractor {

    public Iterable<Image> extract(ItemDetailType detail) {
        ImmutableSet.Builder<Image> images = ImmutableSet.builder();
        ImageListType imageList = detail.getImageList();
        if (imageList != null) {
            for (ImageType image : imageList.getImage()) {
                images.add(extractImage(image));
            }
        }
        return images.build();
    }

    private Image extractImage(ImageType image) {
        return new Image(image.getFilename());
    }
    
}
