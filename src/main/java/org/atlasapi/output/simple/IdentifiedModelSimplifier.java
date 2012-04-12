package org.atlasapi.output.simple;

import java.math.BigInteger;
import java.util.Set;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.simple.Aliased;
import org.atlasapi.media.entity.simple.PublisherDetails;
import org.atlasapi.output.Annotation;

import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;

public abstract class IdentifiedModelSimplifier<F extends Identified, T extends Aliased> implements ModelSimplifier<F, T> {

    protected final SubstitutionTableNumberCodec idCodec = new SubstitutionTableNumberCodec();
    
    protected void copyIdentifiedAttributesTo(Identified identified, Aliased aliased, Set<Annotation> annotations) {
        
        aliased.setUri(identified.getCanonicalUri());
        if (identified.getId() != null) {
            aliased.setId(idCodec.encode(BigInteger.valueOf(identified.getId())));
        }
        
        if(annotations.contains(Annotation.DESCRIPTION) || annotations.contains(Annotation.EXTENDED_DESCRIPTION)) {
            aliased.setAliases(identified.getAliases());
            aliased.setCurie(identified.getCurie());
        }
    }

    protected PublisherDetails toPublisherDetails(Publisher publisher) {

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
