package org.atlasapi.output.simple;

import java.util.Set;

import org.atlasapi.media.entity.EntityType;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.simple.Aliased;
import org.atlasapi.output.Annotation;

public abstract class IdentifiedModelSimplifier<F extends Identified, T extends Aliased> implements ModelSimplifier<F, T> {

    protected void copyIdentifiedAttributesTo(F identified, T aliased, Set<Annotation> annotations) {
        
        aliased.setUri(identified.getCanonicalUri());
        
        if(annotations.contains(Annotation.DESCRIPTION) || annotations.contains(Annotation.EXTENDED_DESCRIPTION)) {
            aliased.setAliases(identified.getAliases());
            aliased.setCurie(identified.getCurie());
        }
    }

}
