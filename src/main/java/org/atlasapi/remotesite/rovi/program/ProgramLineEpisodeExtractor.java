package org.atlasapi.remotesite.rovi.program;

import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForProgram;
import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForSeason;

import java.io.IOException;
import java.util.Collection;

import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.KeyedFileIndex;
import org.atlasapi.remotesite.rovi.series.RoviEpisodeSequenceLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;

/*
 * Extracts an {@link Episode} from a {@link RoviProgramLine} with {@link RoviShowType} SE (Series Episode)
 */
public class ProgramLineEpisodeExtractor extends ProgramLineBaseExtractor<RoviProgramLine, Episode> {

    private final KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex;
    
    protected ProgramLineEpisodeExtractor(
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex,
            KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex,
            ContentResolver contentResolver) {
        super(descriptionIndex, contentResolver);
        this.episodeSequenceIndex = episodeSequenceIndex;
    }

    private final static Logger LOG = LoggerFactory.getLogger(ProgramLineEpisodeExtractor.class);
    
    @Override
    protected Episode createContent() {
        return new Episode();
    }

    @Override
    protected Episode addSpecificData(Episode content, RoviProgramLine programLine) {
        try {
            Collection<RoviEpisodeSequenceLine> results = episodeSequenceIndex.getLinesForKey(programLine.getKey());

            RoviEpisodeSequenceLine episodeSequence = Iterables.getFirst(results, null);

            if (episodeSequence != null) {
                content.setTitle(episodeSequence.getEpisodeTitle());
                content.setEpisodeNumber(episodeSequence.getEpisodeSeasonSequence());

                String seriesCanonicalUri = canonicalUriForProgram(episodeSequence.getSeriesId());
                content.setParentRef(new ParentRef(seriesCanonicalUri));

                if (episodeSequence.getSeasonProgramId().isPresent()) {
                    String seasonCanonicalUri = canonicalUriForSeason(episodeSequence.getSeasonProgramId()
                            .get());
                    content.setSeriesRef(new ParentRef(seasonCanonicalUri));
                }
            }
        } catch (IOException e) {
            LOG.error("Error occurred while reading from Episode Sequence index", e);
        }

        return content;
    }
    
    @Override
    protected Logger log() {
        return LOG;
    }

}
