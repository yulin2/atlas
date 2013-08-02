package org.atlasapi.output.simple;

import static org.junit.Assert.*;

import java.util.Set;

import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Image;
import org.atlasapi.media.entity.ImageAspectRatio;
import org.atlasapi.media.entity.Publisher;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;


public class DescribedImageExtractorTest {
    
    private final DescribedImageExtractor extractor
        = new DescribedImageExtractor();
    
    @Test
    public void testReturnsImagesWhenImagesAndImageFieldPresent() {
        
        Episode hasImages = new Episode();
        hasImages.setImages(ImmutableSet.of(new Image("image")));
        hasImages.setImage("imageField");
        
        Set<Image> extractedImages = extractor.getImages(hasImages);
        
        assertEquals(hasImages.getImages(), extractedImages);
        
    }
    
    @Test
    public void testReturnsEmptyImagesWhenImagesEmptyAndImageFieldUnset() {
        
        Episode hasNoImages = new Episode();
        hasNoImages.setImages(ImmutableSet.<Image>of());
        hasNoImages.setImage(null);
        
        Set<Image> extractedImages = extractor.getImages(hasNoImages);
        
        assertTrue(extractedImages.isEmpty());
        
    }
    
    @Test
    public void testReturnsImagesFromImageFieldWhenImageFieldSetAndImagesEmpty() {
        
        Episode hasImageField = new Episode();
        hasImageField.setImages(ImmutableSet.<Image>of());
        hasImageField.setPublisher(Publisher.PA);
        hasImageField.setImage("anImage");
        
        Set<Image> extractedImages = extractor.getImages(hasImageField);

        assertEquals(ImmutableSet.of(new Image("anImage")), extractedImages);
        
    }

    @Test
    public void testReturnsImageWithPopulatedDefaultsFromSource() {
        
        Episode hasImageField = new Episode();
        hasImageField.setImages(ImmutableSet.<Image>of());
        hasImageField.setPublisher(Publisher.BBC);
        hasImageField.setImage("anImage");
        
        Set<Image> extractedImages = extractor.getImages(hasImageField);
        
        Image image = Iterables.getOnlyElement(extractedImages);
        
        assertEquals(new Image("anImage"), image);
        assertEquals(ImageAspectRatio.SIXTEEN_BY_NINE, image.getAspectRatio());
        
    }
    
}

