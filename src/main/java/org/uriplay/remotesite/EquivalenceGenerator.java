package org.uriplay.remotesite;

import java.util.List;

import org.uriplay.media.entity.Content;
import org.uriplay.media.entity.Equiv;

public interface EquivalenceGenerator<CONTENT extends Content> {

    List<Equiv> equivalent(CONTENT content);
    
}
