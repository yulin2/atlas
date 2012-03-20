package org.atlasapi.output.simple;

import java.util.Set;

import org.atlasapi.media.content.Described;
import org.atlasapi.media.content.LookupRef;
import org.atlasapi.media.content.MediaType;
import org.atlasapi.media.content.Specialization;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.output.Annotation;

import com.google.common.collect.Iterables;

public abstract class DescribedModelSimplifier<F extends Described, T extends Description> extends IdentifiedModelSimplifier<F,T> {
    
    protected void copyBasicDescribedAttributes(F content, T simpleDescription, Set<Annotation> annotations) {
        
        copyIdentifiedAttributesTo(content, simpleDescription, annotations);
        
        if (annotations.contains(Annotation.DESCRIPTION) || annotations.contains(Annotation.EXTENDED_DESCRIPTION)) {
            simpleDescription.setPublisher(toPublisherDetails(content.getPublisher()));
            
            simpleDescription.setTitle(content.getTitle());
            simpleDescription.setDescription(content.getDescription());
            
            simpleDescription.setImage(content.getImage());
            simpleDescription.setThumbnail(content.getThumbnail());

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
        }
        
    }

}
