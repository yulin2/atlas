package org.atlasapi.output.simple;

import java.util.Set;

import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.PublisherDetails;
import org.atlasapi.output.Annotation;

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
            simpleDescription.setSameAs(content.getEquivalentTo());
            simpleDescription.setPresentationChannel(content.getPresentationChannel());
        }
        
    }

    private PublisherDetails toPublisherDetails(Publisher publisher) {

        if (publisher == null) {
            return null;
        }
        
        PublisherDetails details = new PublisherDetails(publisher.key());
        
        if (publisher.country() != null) {
            details.setCountry(publisher.country().code());
        }
        
        details.setName(publisher.title());
        return details;
    }
    
}
