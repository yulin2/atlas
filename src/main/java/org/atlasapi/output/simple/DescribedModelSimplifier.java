package org.atlasapi.output.simple;

import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.simple.Description;
import org.atlasapi.media.entity.simple.PublisherDetails;

public abstract class DescribedModelSimplifier<F extends Described, T extends Description> extends IdentifiedModelSimplifier<F,T> {
    
    protected void copyBasicDescribedAttributes(F content, T simpleDescription) {
        copyIdentifiedAttributesTo(content, simpleDescription);
        simpleDescription.setTitle(content.getTitle());
        simpleDescription.setPublisher(toPublisherDetails(content.getPublisher()));
        simpleDescription.setDescription(content.getDescription());
        simpleDescription.setImage(content.getImage());
        simpleDescription.setThumbnail(content.getThumbnail());
        simpleDescription.setGenres(content.getGenres());
        simpleDescription.setTags(content.getTags());
        simpleDescription.setSameAs(content.getEquivalentTo());
        simpleDescription.setPresentationChannel(content.getPresentationChannel());
        
        MediaType mediaType = content.getMediaType();
        if (mediaType != null) {
            simpleDescription.setMediaType(mediaType.toString().toLowerCase());
        }
        if (content.getSpecialization() != null) {
            simpleDescription.setSpecialization(content.getSpecialization().toString().toLowerCase());
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
