package org.atlasapi.remotesite.rovi;

import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForProgram;

import java.nio.charset.Charset;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.deltas.ContentCreator;
import org.atlasapi.remotesite.rovi.deltas.ContentPopulatorSupplier;
import org.atlasapi.remotesite.rovi.program.RoviProgramDescriptionLine;
import org.atlasapi.remotesite.rovi.program.RoviProgramLine;
import org.atlasapi.remotesite.rovi.series.RoviEpisodeSequenceLine;
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
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex,
            KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex,
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
    protected boolean isToProcess(RoviProgramLine parsedLine) {
        return isToProcess.apply(parsedLine);
    }

    @Override
    protected void populateContent(Content content, RoviProgramLine parsedLine) throws IndexAccessException {
        contentPopulator.populateFromProgramAndAuxiliary(content, parsedLine);
    }

    @Override
    protected Content createContent(RoviProgramLine parsedLine) {
        return ContentCreator.createContent(parsedLine.getShowType().get());
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
