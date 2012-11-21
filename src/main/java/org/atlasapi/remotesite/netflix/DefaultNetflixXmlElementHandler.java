package org.atlasapi.remotesite.netflix;

import org.atlasapi.media.entity.Content;
import org.atlasapi.remotesite.ContentExtractor;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import nu.xom.Element;

public class DefaultNetflixXmlElementHandler implements NetflixXmlElementHandler {

    private final ContentExtractor<Element, Optional<Content>> extractor;
    private final SetMultimap<String, Content> cached = HashMultimap.create();
    
    public DefaultNetflixXmlElementHandler(ContentExtractor<Element, Optional<Content>> extractor) {
        this.extractor = extractor;
    }

    @Override
    public void prepare() {
    }

    @Override
    public void handle(Element element) {
        Optional<Content> possibleContent = extractor.extract(element);
        if (!possibleContent.isPresent()) {
            return;
        }
    }

    @Override
    public void finish() {
        // TODO Auto-generated method stub
        
    }

}
