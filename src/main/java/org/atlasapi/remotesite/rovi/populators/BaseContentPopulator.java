package org.atlasapi.remotesite.rovi.populators;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.not;
import static org.atlasapi.remotesite.rovi.RoviCanonicalUriGenerator.canonicalUriForProgram;
import static org.atlasapi.remotesite.rovi.RoviPredicates.HAS_PARENT;
import static org.atlasapi.remotesite.rovi.RoviPredicates.IS_DELETE;

import java.util.Locale;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.LocalizedDescription;
import org.atlasapi.media.entity.LocalizedTitle;
import org.atlasapi.media.entity.LookupRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.model.RoviCulture;
import org.atlasapi.remotesite.rovi.model.RoviProgramDescriptionLine;
import org.atlasapi.remotesite.rovi.model.RoviProgramLine;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;


public class BaseContentPopulator<CONTENT extends Content> implements ContentPopulator<CONTENT> {

    protected final Optional<RoviProgramLine> optionalProgram;
    private final Iterable<RoviProgramDescriptionLine> roviDescriptions;
    private final ContentResolver contentResolver;
    
    public BaseContentPopulator(Optional<RoviProgramLine> program,
            Iterable<RoviProgramDescriptionLine> descriptions, ContentResolver contentResolver) {
        checkArgument(!isDelete(program), "It's not possible to populate from a program delete action");
        
        this.optionalProgram = checkNotNull(program);
        this.roviDescriptions = checkNotNull(descriptions);
        this.contentResolver = checkNotNull(contentResolver);
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
        for (RoviProgramDescriptionLine roviDescription: roviDescriptions) {
            if (!roviDescription.getSourceId().isPresent()) {
                setLocalizedDescription(content, roviDescription);
            }
        }
        
        cleanEmptyDescriptions(content);
    }

    private void cleanEmptyDescriptions(CONTENT content) {
        Predicate<LocalizedDescription> hasAtLeastOneDescription = new Predicate<LocalizedDescription>() {
            @Override
            public boolean apply(LocalizedDescription description) {
                return 
                    !Strings.isNullOrEmpty(description.getShortDescription()) ||
                    !Strings.isNullOrEmpty(description.getMediumDescription()) ||
                    !Strings.isNullOrEmpty(description.getLongDescription());
            }
        };
        
        content.setLocalizedDescriptions(Iterables.filter(content.getLocalizedDescriptions(),
                hasAtLeastOneDescription));
    }

    private void populateFromProgram(CONTENT content, RoviProgramLine program) {
        if (content.getCanonicalUri() != null) {
            checkArgument(canonicalUriForProgram(program.getProgramId()).equals(content.getCanonicalUri()),
                    "Mapping a line to a content with a different identifier (canonicalUri) is not permitted");
        }
        
        if (program.getLongTitle().isPresent()) {
            setTitle(content, program.getLongTitle().get(), program.getLanguage());
        }
        
        content.setCanonicalUri(canonicalUriForProgram(program.getProgramId()));
        content.setLanguages(Sets.newHashSet(program.getLanguage()));
        
        if (program.getReleaseYear().isPresent()) {
            content.setYear(program.getReleaseYear().get());
        }       
        
        setExplicitEquivalence(content, program);
        
        if (content.getPublisher() == null) {
            content.setPublisher(getPublisherForLanguage(program.getLanguage()));
        }
    }

    private void setTitle(CONTENT content, String title, String language) {
        content.setTitle(title);
        
        LocalizedTitle localizedTitle = new LocalizedTitle();
        localizedTitle.setLocale(new Locale(language));
        localizedTitle.setTitle(title);
        
        content.setLocalizedTitles(ImmutableSet.of(localizedTitle));
    }
    
    private void setExplicitEquivalence(CONTENT content, RoviProgramLine program) {
        if (not(HAS_PARENT).apply(program)) {
            content.setEquivalentTo(ImmutableSet.<LookupRef>of());
            return;
        } 

        String parentCanonicalUri = canonicalUriForProgram(program.getTitleParentId().get());
        Maybe<Identified> maybeParent = contentResolver.findByCanonicalUris(ImmutableList.of(parentCanonicalUri))
                .getFirstValue();

        if (maybeParent.hasValue()) {
            LookupRef parentLookupRef = LookupRef.from((Described) maybeParent.requireValue());
            content.setEquivalentTo(ImmutableSet.of(parentLookupRef));
        }
    }
    
    private void setLocalizedDescription(CONTENT content, RoviProgramDescriptionLine roviDescription) {
        Locale descriptionLocale = RoviCulture.localeFromCulture(roviDescription.getDescriptionCulture());
        Optional<LocalizedDescription> localizedDescription = content.getLocalizedDescription(descriptionLocale);
        
        if (localizedDescription.isPresent()) {
            setRoviDescription(localizedDescription.get(), roviDescription);
        } else {
            content.addLocalizedDescription(createLocalizedDescription(roviDescription));
        }
    }
    
    private void setRoviDescription(LocalizedDescription localizedDescription,
            RoviProgramDescriptionLine roviDescription) {
        
        if (roviDescription.getDescriptionType().equals("Generic Description")) {
            localizedDescription.setShortDescription(roviDescription.getDescription().orNull());
        } else if (roviDescription.getDescriptionType().equals("Plot Synopsis")) {
            localizedDescription.setMediumDescription(roviDescription.getDescription().orNull());
        } else if (roviDescription.getDescriptionType().equals("Synopsis")) {
            localizedDescription.setLongDescription(roviDescription.getDescription().orNull());
        }

        Optional<String> longestDescription = getLongestDescription(localizedDescription);
        localizedDescription.setDescription(longestDescription.orNull());
    }
    
    private LocalizedDescription createLocalizedDescription(RoviProgramDescriptionLine roviDescription) {
        LocalizedDescription localizedDescription = new LocalizedDescription();
        localizedDescription.setLocale(RoviCulture.localeFromCulture(roviDescription.getDescriptionCulture()));
        setRoviDescription(localizedDescription, roviDescription);
        
        return localizedDescription;
    }
    
    private Optional<String> getLongestDescription(LocalizedDescription localizedDescription) {
        if (localizedDescription.getLongDescription() != null) {
            return Optional.of(localizedDescription.getLongDescription());
        } 
        
        if (localizedDescription.getMediumDescription() != null) {
            return Optional.of(localizedDescription.getMediumDescription());
        }
        
        if (localizedDescription.getShortDescription() != null) {
            return Optional.of(localizedDescription.getShortDescription());
        }
        
        return Optional.absent();
    }
    
    public static Publisher getPublisherForLanguage(String language) {
        return Publisher.valueOf("ROVI_" + language.toUpperCase());
    }
    
}
