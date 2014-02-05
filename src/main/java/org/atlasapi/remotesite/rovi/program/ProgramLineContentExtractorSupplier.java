package org.atlasapi.remotesite.rovi.program;

import org.atlasapi.media.entity.Content;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.rovi.KeyedFileIndex;
import org.atlasapi.remotesite.rovi.RoviShowType;
import org.atlasapi.remotesite.rovi.series.RoviSeriesLine;

public class ProgramLineContentExtractorSupplier {

    private final KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex;
    private final KeyedFileIndex<String, RoviSeriesLine> seriesIndex;

    public ProgramLineContentExtractorSupplier(
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex,
            KeyedFileIndex<String, RoviSeriesLine> seriesIndex) {
        this.descriptionIndex = descriptionIndex;
        this.seriesIndex = seriesIndex;
    }

    public ContentExtractor<RoviProgramLine, ? extends Content> getContentExtractor(
            RoviShowType programType) {

        switch (programType) {
        case MO:
            return new ProgramLineFilmExtractor(descriptionIndex);
        case SE:
            return new ProgramLineEpisodeExtractor(descriptionIndex);
        case SM:
            return new ProgramLineBrandExtractor(descriptionIndex, seriesIndex);
        case OT:
            return new ProgramLineItemExtractor(descriptionIndex);
        default:
            throw new RuntimeException("Program type not supported");
        }

    }

}