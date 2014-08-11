package org.atlasapi.remotesite.btvod;

import com.google.common.base.Optional;


public interface ImageUriProvider {

    Optional<String> imageUriFor(String productId);
    
}
