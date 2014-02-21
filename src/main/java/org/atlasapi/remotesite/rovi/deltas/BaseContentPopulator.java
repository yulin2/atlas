package org.atlasapi.remotesite.rovi.deltas;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.atlasapi.remotesite.rovi.ActionType.DELETE;
import static org.atlasapi.remotesite.rovi.CultureToPublisherMap.culturesOrdering;
import static org.atlasapi.remotesite.rovi.CultureToPublisherMap.isCultureGoodForPublisher;
import static org.atlasapi.remotesite.rovi.RoviPredicates.HAS_PARENT;
import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForProgram;
import static org.atlasapi.remotesite.rovi.RoviUtils.getPublisherForLanguageAndCulture;

import java.util.List;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.program.RoviProgramDescriptionLine;
import org.atlasapi.remotesite.rovi.program.RoviProgramLine;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;


public class BaseContentPopulator<CONTENT extends Content> implements ContentPopulator<CONTENT> {

    protected final Optional<RoviProgramLine> optionalProgram;
    private final Iterable<RoviProgramDescriptionLine> descriptions;
    private final ContentResolver contentResolver;
    
    public BaseContentPopulator(Optional<RoviProgramLine> program,
            Iterable<RoviProgramDescriptionLine> descriptions, ContentResolver contentResolver) {
        checkArgument(isNotDelete(program), "It's not possible to populate from a program delete action");
        checkNotNull(descriptions);
        checkNotNull(contentResolver);
        
        this.optionalProgram = program;
        this.descriptions = descriptions;
        this.contentResolver = contentResolver;
    }
    
    private boolean isNotDelete(Optional<RoviProgramLine> program) {
        return program.isPresent() && !program.get().getActionType().equals(DELETE);
    }

    @Override
    public void populateContent(CONTENT content) {
        if (optionalProgram.isPresent()) {
            populateFromProgram(content, optionalProgram.get());
        }
        
        handleDescriptions(content);
        addSpecificData(content);
    }

    protected void addSpecificData(CONTENT content) {
        // Do nothing;
    }

    private void handleDescriptions(CONTENT content) {
        for (RoviProgramDescriptionLine description: descriptions) {
            if (!description.getSourceId().isPresent() && isCultureGoodForPublisher(description.getDescriptionCulture(), content.getPublisher())) {
                setContentDescription(content, description);
            }
        }
    }

    private void populateFromProgram(CONTENT content, RoviProgramLine program) {
        checkArgument(checkIdCompatibility(content, program), "Mapping a line to a content with a different identifier (canonicalUri) is not permitted");
        
        if (program.getLongTitle().isPresent()) {
            content.setTitle(program.getLongTitle().get());
        }
        
        content.setCanonicalUri(canonicalUriForProgram(program.getProgramId()));
        content.setLanguages(Sets.newHashSet(program.getLanguage()));
        
        if (program.getReleaseYear().isPresent()) {
            content.setYear(program.getReleaseYear().get());
        }       
        
        setParentIfNeeded(content, program);
        
        if (content.getPublisher() == null) {
            content.setPublisher(getPublisherForLanguageAndCulture(program.getLanguage(), calculateCulture(descriptions)));
        }
    }
    
    private boolean checkIdCompatibility(CONTENT content, RoviProgramLine line) {
        return content.getCanonicalUri() == null
            || content.getCanonicalUri().equals(canonicalUriForProgram(line.getProgramId()));
    }
    
    private void setParentIfNeeded(CONTENT content, RoviProgramLine program) {
        if (HAS_PARENT.apply(program)) {
            String parentCanonicalUri = canonicalUriForProgram(program.getTitleParentId().get());
            Maybe<Identified> maybeParent = contentResolver.findByCanonicalUris(ImmutableList.of(parentCanonicalUri)).getFirstValue();
            
            if (maybeParent.hasValue()) {
                LookupRef parentLookupRef = LookupRef.from((Described) maybeParent.requireValue());
                content.setEquivalentTo(ImmutableSet.of(parentLookupRef));
            }
        }
    }
    
    private Optional<String> calculateCulture(Iterable<RoviProgramDescriptionLine> descriptions) {
        Multimap<String, RoviProgramDescriptionLine> descriptionsByCulture = HashMultimap.create();
        
        for (RoviProgramDescriptionLine description: descriptions) {
            if (!description.getSourceId().isPresent()) {
                descriptionsByCulture.put(description.getDescriptionCulture(), description);
            }
        }      
        
        if (!descriptionsByCulture.isEmpty()) {
            List<String> sortedCultures = culturesOrdering().sortedCopy(descriptionsByCulture.keys());
            String firstCulture = sortedCultures.iterator().next();
            return Optional.of(firstCulture);
        }
        
        return Optional.absent();
    }
    
    private void setContentDescription(CONTENT content, RoviProgramDescriptionLine description) {
        if (description.getDescriptionType().equals("Generic Description")) {
            content.setShortDescription(description.getDescription().orNull());
        } else if (description.getDescriptionType().equals("Plot Synopsis")) {
            content.setMediumDescription(description.getDescription().orNull());
        } else if (description.getDescriptionType().equals("Synopsis")) {
            content.setLongDescription(description.getDescription().orNull());
        }
        
        Optional<String> longestDescription = getLongestDescription(content);
        if (longestDescription.isPresent()) {
            content.setDescription(longestDescription.get());
        }
        
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
    
}
