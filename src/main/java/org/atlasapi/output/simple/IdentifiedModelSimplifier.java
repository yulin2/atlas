package org.atlasapi.output.simple;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.simple.Aliased;

import com.google.common.base.Function;

public abstract class IdentifiedModelSimplifier<F extends Identified, T extends Aliased> implements Function<F, T> {

    protected void copyIdentifiedAttributesTo(F identified, T aliased) {
        aliased.setUri(identified.getCanonicalUri());
        aliased.setAliases(identified.getAliases());
        aliased.setCurie(identified.getCurie());
    }

}
