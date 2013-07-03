package org.atlasapi.remotesite.talktalk;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.atlasapi.media.entity.Image;
import org.atlasapi.remotesite.talktalk.vod.bindings.ImageListType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ImageType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
import org.junit.Test;

import com.google.common.collect.Iterables;


public class TalkTalkImagesExtractorTest {
    
    private final TalkTalkImagesExtractor extractor = new TalkTalkImagesExtractor();
    
    @Test
    public void testExtractingImages() {
        ItemDetailType detail = new ItemDetailType();
        ImageListType imageList = new ImageListType();
        ImageType imageType = new ImageType();
        imageType.setFilename("image.png");
        imageList.getImage().add(imageType);
        detail.setImageList(imageList);        
        Iterable<Image> extracted = extractor.extract(detail);
        
        assertThat(Iterables.getOnlyElement(extracted).getCanonicalUri(), is("image.png"));
    }
    
}
