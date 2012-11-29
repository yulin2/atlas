package org.atlasapi.output.simple;

import java.util.Set;

import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.ImageType;
import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Image;
import org.atlasapi.output.Annotation;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.media.MimeType;

public abstract class DescribedModelSimplifier<F extends Described, T extends Description> extends IdentifiedModelSimplifier<F,T> {
    
    protected void copyBasicDescribedAttributes(F content, T simpleDescription, Set<Annotation> annotations) {
        
        copyIdentifiedAttributesTo(content, simpleDescription, annotations);
        
        if (annotations.contains(Annotation.DESCRIPTION) || annotations.contains(Annotation.EXTENDED_DESCRIPTION)) {
            simpleDescription.setPublisher(toPublisherDetails(content.getPublisher()));
            
            simpleDescription.setTitle(content.getTitle());
            simpleDescription.setDescription(content.getDescription());
            simpleDescription.setImage(content.getImage());
            simpleDescription.setImages(toImages(content.getImages()));
            simpleDescription.setThumbnail(content.getThumbnail());
            simpleDescription.setShortDescription(content.getShortDescription());

            MediaType mediaType = content.getMediaType();
            if (mediaType != null) {
                simpleDescription.setMediaType(mediaType.toString().toLowerCase());
            }
            
            Specialization specialization = content.getSpecialization();
            if (specialization != null) {
                simpleDescription.setSpecialization(specialization.toString().toLowerCase());
            }
        }
        
        if (annotations.contains(Annotation.EXTENDED_DESCRIPTION)) {
            simpleDescription.setGenres(content.getGenres());
            simpleDescription.setTags(content.getTags());
            simpleDescription.setSameAs(Iterables.transform(content.getEquivalentTo(),LookupRef.TO_ID));
            simpleDescription.setPresentationChannel(content.getPresentationChannel());
            simpleDescription.setMediumDescription(content.getMediumDescription());
            simpleDescription.setLongDescription(content.getLongDescription());
            
        }
        
    }

    private Iterable<Image> toImages(Iterable<org.atlasapi.media.entity.Image> images) {
        Builder<Image> simpleImages = ImmutableSet.builder();
        for(org.atlasapi.media.entity.Image image : images) {
            Image simpleImage = new Image(image.getCanonicalUri());
            simpleImage.setCopyright(image.getCopyright());
            simpleImage.setPublisher(toPublisherDetails(image.getPublisher()));
            simpleImage.setWidth(image.getWidth());
            simpleImage.setHeight(image.getHeight());
            if(ImageType.SIXTEEN_BY_NINE.equals(image.getType())) {
                simpleImage.setType("16x9");
                simpleImage.setFormat(MimeType.IMAGE_JPG.toString());
            }
            simpleImages.add(simpleImage);
        }
        return simpleImages.build();
    }

}
