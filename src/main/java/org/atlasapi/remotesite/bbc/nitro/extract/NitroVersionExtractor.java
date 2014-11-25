package org.atlasapi.remotesite.bbc.nitro.extract;

import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.media.entity.Version;


public class NitroVersionExtractor implements ContentExtractor<com.metabroadcast.atlas.glycerin.model.Version, Version> {

    @Override public Version extract(com.metabroadcast.atlas.glycerin.model.Version version) {
        return new Version();
    }
}
