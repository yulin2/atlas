package org.atlasapi.remotesite;

import java.util.List;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Equiv;

public interface EquivGenerator<CONTENT extends Content> {

    List<Equiv> equivalent(CONTENT content);
    
}
