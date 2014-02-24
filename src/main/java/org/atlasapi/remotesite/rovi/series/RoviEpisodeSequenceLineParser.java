package org.atlasapi.remotesite.rovi.series;

import static org.atlasapi.remotesite.rovi.RoviConstants.LINE_SPLITTER;
import static org.atlasapi.remotesite.rovi.RoviUtils.getIntPartAtPosition;
import static org.atlasapi.remotesite.rovi.RoviUtils.getPartAtPosition;

import org.atlasapi.remotesite.rovi.RoviLineParser;


public class RoviEpisodeSequenceLineParser implements RoviLineParser<RoviEpisodeSequenceLine>{

    private static final int SERIES_ID_POS = 0;
    private static final int SEASON_PROGRAM_ID_POS = 1;
    private static final int PROGRAM_ID_POS = 2;
    private static final int EPISODE_TITLE_POS = 3;
    private static final int EPISODE_SEASON_NUMBER_POS = 4;
    private static final int EPISODE_SEASON_SEQUENCE_POS = 5;
    
    @Override
    public RoviEpisodeSequenceLine apply(String line) {
        Iterable<String> parts = LINE_SPLITTER.split(line);
        
        RoviEpisodeSequenceLine.Builder builder = RoviEpisodeSequenceLine.builder();
        
        builder.withSeriesId(getPartAtPosition(parts, SERIES_ID_POS));
        builder.withSeasonProgramId(getPartAtPosition(parts, SEASON_PROGRAM_ID_POS));
        builder.withProgramId(getPartAtPosition(parts, PROGRAM_ID_POS));
        builder.withEpisodeTitle(getPartAtPosition(parts, EPISODE_TITLE_POS));
        builder.withEpisodeSeasonNumber(getIntPartAtPosition(parts, EPISODE_SEASON_NUMBER_POS));
        builder.withEpisodeSeasonSequence(getIntPartAtPosition(parts, EPISODE_SEASON_SEQUENCE_POS));
        
        return builder.build();
    }

}
