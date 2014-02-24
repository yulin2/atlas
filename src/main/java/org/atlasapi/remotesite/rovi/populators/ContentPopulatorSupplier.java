package org.atlasapi.remotesite.rovi.populators;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.indexing.IndexAccessException;
import org.atlasapi.remotesite.rovi.indexing.KeyedFileIndex;
import org.atlasapi.remotesite.rovi.model.RoviEpisodeSequenceLine;
import org.atlasapi.remotesite.rovi.model.RoviProgramDescriptionLine;
import org.atlasapi.remotesite.rovi.model.RoviProgramLine;

import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;


/**
 * This class populates the content after having picked the proper ContentPopulator for it
 */
// TODO: Better name!
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
        this.descriptionIndex = checkNotNull(descriptionIndex);
        this.episodeSequenceIndex = checkNotNull(episodeSequenceIndex);
        this.contentResolver = checkNotNull(contentResolver);
        this.seasonNumberCache = checkNotNull(seasonNumberCache);
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
