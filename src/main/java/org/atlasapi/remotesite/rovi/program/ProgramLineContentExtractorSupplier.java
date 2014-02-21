package org.atlasapi.remotesite.rovi.program;

import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.KeyedFileIndex;
import org.atlasapi.remotesite.rovi.RoviContentExtractor;
import org.atlasapi.remotesite.rovi.RoviShowType;
import org.atlasapi.remotesite.rovi.series.RoviEpisodeSequenceLine;
import org.atlasapi.remotesite.rovi.series.RoviSeriesLine;

public class ProgramLineContentExtractorSupplier {

    private final KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex;
    private final KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex;
    private final ContentResolver contentResolver;

    public ProgramLineContentExtractorSupplier(
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex,
            KeyedFileIndex<String, RoviSeriesLine> seriesIndex,
            KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex,
            ContentResolver contentResolver) {
        this.descriptionIndex = descriptionIndex;
        this.episodeSequenceIndex = episodeSequenceIndex;
        this.contentResolver = contentResolver;
    }

    public RoviContentExtractor<RoviProgramLine, ? extends Content> getContentExtractor(
            RoviShowType programType) {

        switch (programType) {
            case MOVIE:
                return new ProgramLineFilmExtractor(descriptionIndex, contentResolver);
            case SERIES_EPISODE:
                return new ProgramLineEpisodeExtractor(descriptionIndex, episodeSequenceIndex, contentResolver);
            case SERIES_MASTER:
                return new ProgramLineBrandExtractor(descriptionIndex, contentResolver);
            case OTHER:
                return new ProgramLineItemExtractor(descriptionIndex, contentResolver);
            default:
                throw new RuntimeException("Program type not supported");
        }

    }

}