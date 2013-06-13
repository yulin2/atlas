package org.atlasapi.output.simple;

import java.util.Set;

import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Image;
import org.atlasapi.output.Annotation;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Iterables;

public abstract class DescribedModelSimplifier<F extends Described, T extends Description> extends IdentifiedModelSimplifier<F,T> {
    
    private final ImageSimplifier imageSimplifier;
    
    public DescribedModelSimplifier(ImageSimplifier imageSimplifier) {
        this.imageSimplifier = imageSimplifier;
    }
    
    protected void copyBasicDescribedAttributes(F content, T simpleDescription, Set<Annotation> annotations) {
        
        copyIdentifiedAttributesTo(content, simpleDescription, annotations);
        
        if (annotations.contains(Annotation.DESCRIPTION) || annotations.contains(Annotation.EXTENDED_DESCRIPTION)) {
            simpleDescription.setPublisher(toPublisherDetails(content.getPublisher()));
            
            simpleDescription.setTitle(content.getTitle());
            simpleDescription.setDescription(content.getDescription());
            simpleDescription.setImage(content.getImage());
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
        
        if (annotations.contains(Annotation.IMAGES)) {
            simpleDescription.setImages(toImages(content.getImages(), annotations));
        }
        
    }

    private Iterable<Image> toImages(Iterable<org.atlasapi.media.entity.Image> images, Set<Annotation> annotations) {
        Builder<Image> simpleImages = ImmutableSet.builder();
        for(org.atlasapi.media.entity.Image image : images) {
            simpleImages.add(imageSimplifier.simplify(image, annotations, null));
        }
        return simpleImages.build();
    }
}
