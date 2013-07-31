package org.atlasapi.output.simple;

import java.math.BigInteger;
import java.util.Set;

import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.Image;
import org.atlasapi.media.entity.simple.SameAs;
import org.atlasapi.output.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.ids.NumberToShortStringCodec;

public abstract class DescribedModelSimplifier<F extends Described, T extends Description> extends IdentifiedModelSimplifier<F,T> {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final ImageSimplifier imageSimplifier;
    
    protected DescribedModelSimplifier(ImageSimplifier imageSimplifier) {
        this.imageSimplifier = imageSimplifier;
    }
    
    protected DescribedModelSimplifier(ImageSimplifier imageSimplifier, NumberToShortStringCodec codec) {
        super(codec);
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
            simpleDescription.setSameAs(Iterables.transform(content.getEquivalentTo(),LookupRef.TO_URI));
            simpleDescription.setEquivalents(Iterables.transform(content.getEquivalentTo(), TO_SAME_AS));
            simpleDescription.setPresentationChannel(content.getPresentationChannel());
            simpleDescription.setMediumDescription(content.getMediumDescription());
            simpleDescription.setLongDescription(content.getLongDescription());
            
        }
        
        if (annotations.contains(Annotation.IMAGES)) {
            if (content.getImages().isEmpty() && !Strings.isNullOrEmpty(content.getImage())) {
                simpleDescription.setImages(ImmutableList.of(new Image(content.getImage())));
            } else {
                simpleDescription.setImages(toImages(content.getImages(), annotations));
            }
        }
        
    }

    private Iterable<Image> toImages(Iterable<org.atlasapi.media.entity.Image> images, Set<Annotation> annotations) {
        Builder<Image> simpleImages = ImmutableSet.builder();
        for(org.atlasapi.media.entity.Image image : images) {
            simpleImages.add(imageSimplifier.simplify(image, annotations, null));
        }
        return simpleImages.build();
    }
    
    private Function<LookupRef, SameAs> TO_SAME_AS = new Function<LookupRef, SameAs>() {

        @Override
        public SameAs apply(LookupRef input) {
            Long id = input.id();
            if (id == null) {
                log.info("null id for {}", input);
            }
            return new SameAs(id != null ? idCodec.encode(BigInteger.valueOf(id)) : null, input.uri());
        }
    };
}
