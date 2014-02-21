package org.atlasapi.remotesite.rovi.deltas;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.IndexAccessException;
import org.atlasapi.remotesite.rovi.KeyedFileIndex;
import org.atlasapi.remotesite.rovi.program.RoviProgramDescriptionLine;
import org.atlasapi.remotesite.rovi.program.RoviProgramLine;
import org.atlasapi.remotesite.rovi.series.RoviEpisodeSequenceLine;

import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;


public class ContentPopulatorSupplier {

    private final KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex;
    private final KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex;
    private final ContentResolver contentResolver;
    private final LoadingCache<String, Optional<Integer>> seasonNumberCache;

    public ContentPopulatorSupplier(
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex,
            KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex,
            ContentResolver contentResolver,
            LoadingCache<String, Optional<Integer>> seasonNumberCache) {
        this.descriptionIndex = descriptionIndex;
        this.episodeSequenceIndex = episodeSequenceIndex;
        this.contentResolver = contentResolver;
        this.seasonNumberCache = seasonNumberCache;
    }

    public void populateFromProgramAndAuxiliary(Content content, RoviProgramLine program) throws IndexAccessException {
        populateContent(content, Optional.of(program), program.getProgramId());
    }

    public void populateFromAuxiliaryOnly(Content content, String programId) throws IndexAccessException {
        populateContent(content, Optional.<RoviProgramLine>absent(), programId);
    }

    private void populateContent(Content content, Optional<RoviProgramLine> program, String programId)
            throws IndexAccessException {
        if (content instanceof Episode) {
            EpisodePopulator populator = new EpisodePopulator(
                    program,
                    descriptionIndex.getLinesForKey(programId),
                    contentResolver,
                    episodeSequenceIndex.getFirstForKey(programId),
                    seasonNumberCache);
            
            populator.populateContent((Episode) content);
        } else if (content instanceof Item) {
            ItemPopulator<Item> populator = new ItemPopulator<Item>(
                    program,
                    descriptionIndex.getLinesForKey(programId),
                    contentResolver);
            
            populator.populateContent((Item) content);            
        } else {
            BaseContentPopulator<Content> populator = new BaseContentPopulator<Content>(
                    program,
                    descriptionIndex.getLinesForKey(programId),
                    contentResolver);
            
            populator.populateContent(content);
        }
    }
    
}
