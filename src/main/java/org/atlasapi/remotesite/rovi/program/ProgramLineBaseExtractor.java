package org.atlasapi.remotesite.rovi.program;

import static org.atlasapi.remotesite.rovi.RoviPredicates.HAS_PARENT;
import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForProgram;
import static org.atlasapi.remotesite.rovi.RoviUtils.getPublisherForLanguage;
import static org.atlasapi.remotesite.rovi.RoviUtils.getPublisherForLanguageAndCulture;

import java.util.Collection;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.IndexAccessException;
import org.atlasapi.remotesite.rovi.KeyedFileIndex;
import org.atlasapi.remotesite.rovi.RoviContentExtractor;
import org.slf4j.Logger;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;

/*
 * Base Extractor that sets common fields on a {@link Content} from a {@link RoviProgramLine}
 */
public abstract class ProgramLineBaseExtractor<SOURCE, CONTENT extends Content> implements RoviContentExtractor<RoviProgramLine, CONTENT> {

    private final KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex;
    private final ContentResolver contentResolver;
    
    protected ProgramLineBaseExtractor(KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex, ContentResolver contentResolver) {
        this.descriptionIndex = descriptionIndex;
        this.contentResolver = contentResolver;
    }
    
    @Override
    public CONTENT extract(RoviProgramLine roviLine) throws IndexAccessException {
        CONTENT content = createContent();
        
        content.setTitle(roviLine.getLongTitle());
        content.setCanonicalUri(canonicalUriForProgram(roviLine.getProgramId()));
        content.setPublisher(getPublisherForLanguage(roviLine.getLanguage()));
        content.setLanguages(Sets.newHashSet(roviLine.getLanguage()));
        
        if (roviLine.getReleaseYear().isPresent()) {
            content.setYear(roviLine.getReleaseYear().get());
        }
        
        Optional<String> descriptionCulture = Optional.absent();
        descriptionCulture = setDescriptionAndGetCulture(content, roviLine);
        
        content.setPublisher(getPublisherForLanguageAndCulture(roviLine.getLanguage(), descriptionCulture));
        setParentIfNeeded(roviLine, content);
        createVersionIfNeeded(content, roviLine);
        
        CONTENT extracted = addSpecificData(content, roviLine);
        log().trace("Extracted {} with canonical uri {}", extracted.getClass().getName(), extracted.getCanonicalUri());
        
        return extracted;
    }

    private void setParentIfNeeded(RoviProgramLine roviLine, CONTENT content) {
        if (HAS_PARENT.apply(roviLine)) {
            String parentCanonicalUri = canonicalUriForProgram(roviLine.getTitleParentId().get());
            Maybe<Identified> maybeParent = contentResolver.findByCanonicalUris(ImmutableList.of(parentCanonicalUri)).getFirstValue();
            
            if (maybeParent.hasValue()) {
                LookupRef parentLookupRef = LookupRef.from((Described) maybeParent.requireValue());
                content.setEquivalentTo(ImmutableSet.of(parentLookupRef));
            }
        }
    }
    
    protected abstract Logger log();
    protected abstract CONTENT createContent();
    protected abstract CONTENT addSpecificData(CONTENT content, RoviProgramLine roviLine) throws IndexAccessException;
    
    private Optional<String> setDescriptionAndGetCulture(CONTENT content, RoviProgramLine roviLine) throws IndexAccessException {
        Optional<String> descriptionCulture = Optional.absent();
        Collection<RoviProgramDescriptionLine> descriptions = descriptionIndex.getLinesForKey(roviLine.getKey());
        
        Multimap<Optional<String>, RoviProgramDescriptionLine> descriptionsBySource = HashMultimap.create();
        
        for (RoviProgramDescriptionLine description: descriptions) {
            descriptionsBySource.put(description.getSourceId(), description);
        }
        
        Collection<RoviProgramDescriptionLine> descriptionsWithoutSource = descriptionsBySource.get(Optional.<String>absent());
        
        Multimap<String, RoviProgramDescriptionLine> descriptionsByCulture = HashMultimap.create();
        
        for (RoviProgramDescriptionLine description: descriptionsWithoutSource) {
            descriptionsByCulture.put(description.getDescriptionCulture(), description);
        }
        
        if (!descriptionsByCulture.isEmpty()) {
            // TODO: Improve, taking the first culture for now
            String firstCulture = descriptionsByCulture.keySet().iterator().next();
            descriptionCulture = Optional.of(firstCulture);
            
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
        
        return descriptionCulture;
    }

    private Optional<String> getLongestDescription(CONTENT content) {
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
    
    private void createVersionIfNeeded(CONTENT content, RoviProgramLine roviLine) {
        if (content instanceof Item) {
            Item item = (Item) content;
            Version version = new Version();
            version.setDuration(roviLine.getDuration());
            item.setVersions(Sets.newHashSet(version));
        }
    }
    
    
}
