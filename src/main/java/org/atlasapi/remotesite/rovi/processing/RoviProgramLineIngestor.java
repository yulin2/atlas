package org.atlasapi.remotesite.rovi.processing;

import static com.google.common.base.Preconditions.checkArgument;
import static org.atlasapi.remotesite.rovi.RoviCanonicalUriGenerator.canonicalUriForProgram;

import java.nio.charset.Charset;
import java.util.Set;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.RoviContentWriter;
import org.atlasapi.remotesite.rovi.indexing.IndexAccessException;
import org.atlasapi.remotesite.rovi.model.RoviProgramLine;
import org.atlasapi.remotesite.rovi.model.RoviShowType;
import org.atlasapi.remotesite.rovi.parsers.RoviLineParser;
import org.atlasapi.remotesite.rovi.populators.ContentPopulatorSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;


public class RoviProgramLineIngestor extends RoviActionLineIngestor<RoviProgramLine, Content> {
    
    private final static Logger LOG = LoggerFactory.getLogger(RoviProgramLineIngestor.class); 
    
    private final Predicate<? super RoviProgramLine> isToProcess;
    private final ContentResolver contentResolver;
    private final ContentPopulatorSupplier contentPopulator;

    public RoviProgramLineIngestor(RoviLineParser<RoviProgramLine> parser, Charset charset,
            Predicate<? super RoviProgramLine> isToProcess,
            RoviContentWriter contentWriter,
            ContentResolver contentResolver,
            ContentPopulatorSupplier contentPopulator) {
        super(parser, charset, contentWriter);
        this.isToProcess = isToProcess;
        this.contentResolver = contentResolver;
        this.contentPopulator = contentPopulator;
    }

    @Override
    protected Logger log() {
        return LOG;
    }

    @Override
    protected boolean shouldProcess(RoviProgramLine parsedLine) {
        return isToProcess.apply(parsedLine);
    }

    @Override
    protected void populateContent(Content content, RoviProgramLine parsedLine) throws IndexAccessException {
        // Breaking if the type of the program is different from the one we already have in the database
        ensureContentMatchesShowType(content, parsedLine);
        
        contentPopulator.populateFromProgramAndAuxiliary(content, parsedLine);
    }

    private void ensureContentMatchesShowType(Content content, RoviProgramLine parsedLine) {
        RoviShowType showType = parsedLine.getShowType().get();
        checkArgument(ContentCreator.hasCorrectType(content, showType), "The content type [" + content.getClass().getName() + "] doesn't match with the show type [" + showType + "]");
    }

    @Override
    protected Content createContent(RoviProgramLine parsedLine) {
        Content created = ContentCreator.createContent(parsedLine.getShowType().get());
        
        if (created instanceof Item) {
            setExistentVersionIfItemExists(created, parsedLine);
        }
            
        return created;
    }

    
    /**
     * This method make sure that if we're re-ingesting a program that already exists in our database, 
     * it keeps its original Version and therefore all the broadcasts attached to it 
     * 
     * @param created - the empty content just created
     * @param parsedLine - the line containing program infos
     */
    private void setExistentVersionIfItemExists(Content created, RoviProgramLine parsedLine) {
        Optional<Content> existent = resolveContent(parsedLine);
        
        if (existent.isPresent() && existent.get() instanceof Item) {
            Set<Version> existentVersions = ((Item) existent.get()).getVersions();
            ((Item) created).setVersions(existentVersions);
        }
    }

    @Override
    protected Optional<Content> resolveContent(RoviProgramLine parsedLine) {
        String canonicalUri = canonicalUriForProgram(parsedLine.getProgramId());
        Maybe<Identified> maybeResolved = contentResolver.findByCanonicalUris(ImmutableList.of(canonicalUri)).getFirstValue();
        
        if (maybeResolved.isNothing()) {
            return Optional.absent();
        }
        
        return Optional.of((Content) maybeResolved.requireValue());
    }

}
