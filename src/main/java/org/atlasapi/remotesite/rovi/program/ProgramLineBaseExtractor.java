package org.atlasapi.remotesite.rovi.program;

import static org.atlasapi.remotesite.rovi.RoviPredicates.HAS_PARENT;
import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriFor;
import static org.atlasapi.remotesite.rovi.RoviUtils.getPublisherForLanguage;

import java.io.IOException;
import java.util.Collection;

import org.atlasapi.media.entity.Content;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.rovi.KeyedFileIndex;
import org.atlasapi.remotesite.rovi.RoviPredicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public abstract class ProgramLineBaseExtractor<SOURCE, CONTENT extends Content> implements ContentExtractor<RoviProgramLine, CONTENT> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private final KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex;
    
    protected ProgramLineBaseExtractor(KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex) {
        this.descriptionIndex = descriptionIndex;
    }
    
    @Override
    public CONTENT extract(RoviProgramLine roviLine) {
        CONTENT content = createContent();
        
        content.setTitle(roviLine.getLongTitle());
        content.setCanonicalUri(canonicalUriFor(roviLine.getProgramId()));
        content.setPublisher(getPublisherForLanguage(roviLine.getLanguage()));
        content.setLanguages(Sets.newHashSet(roviLine.getLanguage()));
        
        if (HAS_PARENT.apply(roviLine)) {
            // TODO: 
            // content.setEquivalentTo(...)
        }
        
        try {
            setDescription(content, roviLine);
        } catch (IOException e){
            log.error("Error while trying to populate the description for program " + roviLine.getKey(), e);
        }
        
        return addSpecificData(content, roviLine);
    }
    
    protected abstract CONTENT createContent();
    protected abstract CONTENT addSpecificData(CONTENT content, RoviProgramLine roviLine);
    
    private void setDescription(CONTENT content, RoviProgramLine roviLine) throws IOException {
        Collection<RoviProgramDescriptionLine> descriptions = descriptionIndex.getLinesForKey(roviLine.getKey());
        
        Multimap<String, RoviProgramDescriptionLine> descriptionsByCulture = HashMultimap.create();
        
        for (RoviProgramDescriptionLine description: descriptions) {
            descriptionsByCulture.put(description.getDescriptionCulture(), description);
        }
        
        if (!descriptionsByCulture.isEmpty()) {
            String firstCulture = descriptionsByCulture.keySet().iterator().next();
            // TODO: Improve, taking the first culture for now
            Collection<RoviProgramDescriptionLine> specificDescriptions = descriptionsByCulture.get(firstCulture);
        
            for (RoviProgramDescriptionLine description: specificDescriptions) {
                if (description.getDescriptionType().equals("Generic Description")) {
                    content.setShortDescription(description.getDescription());
                } else if (description.getDescriptionType().equals("Plot Synopsis")){
                    content.setMediumDescription(description.getDescription());
                } else if (description.getDescriptionType().equals("Synopsis") || description.getDescriptionType().equals("Short Synopsis")) {
                    content.setLongDescription(description.getDescription());
                }
            }
        }

        Optional<String> longestDescription = getLongestDescription(content);
        if (longestDescription.isPresent()) {
            content.setDescription(longestDescription.get());
        }
    }
    
    private Optional<String> getLongestDescription(Content content) {
        if (content.getLongDescription() != null) {
            return Optional.of(content.getLongDescription());
        } 
        
        if (content.getMediumDescription() != null) {
            return Optional.of(content.getMediumDescription());
        }
        
        if (content.getShortDescription() != null) {
            return Optional.of(content.getShortDescription());
        }
        
        return Optional.absent();
    }
}
