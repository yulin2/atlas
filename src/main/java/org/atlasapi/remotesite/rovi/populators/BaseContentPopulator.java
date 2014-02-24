package org.atlasapi.remotesite.rovi.populators;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.atlasapi.remotesite.rovi.RoviPredicates.HAS_PARENT;
import static org.atlasapi.remotesite.rovi.RoviPredicates.IS_DELETE;
import static org.atlasapi.remotesite.rovi.RoviCanonicalUriGenerator.canonicalUriForProgram;
import static org.atlasapi.remotesite.rovi.RoviConstants.DEFAULT_PUBLISHER;
import static org.atlasapi.remotesite.rovi.model.CultureToPublisherMap.culturesOrdering;
import static org.atlasapi.remotesite.rovi.model.CultureToPublisherMap.isCultureGoodForPublisher;

import java.util.Collection;
import java.util.List;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.model.CultureToPublisherMap;
import org.atlasapi.remotesite.rovi.model.RoviProgramDescriptionLine;
import org.atlasapi.remotesite.rovi.model.RoviProgramLine;

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
        checkArgument(!isDelete(program), "It's not possible to populate from a program delete action");
        checkNotNull(descriptions);
        checkNotNull(contentResolver);
        
        this.optionalProgram = program;
        this.descriptions = descriptions;
        this.contentResolver = contentResolver;
    }
    
    private boolean isDelete(Optional<RoviProgramLine> program) {
        return program.isPresent() && IS_DELETE.apply(program.get());
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
        
        setExplicitEquivalence(content, program);
        
        if (content.getPublisher() == null) {
            content.setPublisher(getPublisherForLanguageAndCulture(program.getLanguage(), calculateCulture(descriptions)));
        }
    }
    
    private boolean checkIdCompatibility(CONTENT content, RoviProgramLine line) {
        return content.getCanonicalUri() == null
            || content.getCanonicalUri().equals(canonicalUriForProgram(line.getProgramId()));
    }
    
    private void setExplicitEquivalence(CONTENT content, RoviProgramLine program) {
        if (HAS_PARENT.apply(program)) {
            String parentCanonicalUri = canonicalUriForProgram(program.getTitleParentId().get());
            Maybe<Identified> maybeParent = contentResolver.findByCanonicalUris(ImmutableList.of(parentCanonicalUri)).getFirstValue();
            
            if (maybeParent.hasValue()) {
                LookupRef parentLookupRef = LookupRef.from((Described) maybeParent.requireValue());
                content.setEquivalentTo(ImmutableSet.of(parentLookupRef));
            }
        } else {
            content.setEquivalentTo(ImmutableSet.<LookupRef>of());
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
        content.setDescription(longestDescription.orNull());
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
    
    public static Publisher getPublisherForLanguage(String language) {
        return getPublisherForLanguageAndCulture(language, Optional.<String>absent());
    }
    
    
    public static Publisher getPublisherForLanguageAndCulture(String language, Optional<String> descriptionCulture) {
        if (CultureToPublisherMap.getCultures(language).isEmpty()) {
            return Publisher.valueOf("ROVI_" + language.toUpperCase());
        }
        
        if (!descriptionCulture.isPresent()) {
            Optional<String> defaultCulture = CultureToPublisherMap.getDefaultCultureForLanguage(language);
            return CultureToPublisherMap.getPublisher(defaultCulture.get());
        }
        
        Collection<String> cultures = CultureToPublisherMap.getCultures(language);
        if (cultures.contains(descriptionCulture.get())) {
            return CultureToPublisherMap.getPublisher(descriptionCulture.get());
        }
        
        return DEFAULT_PUBLISHER;
    }
    
}
