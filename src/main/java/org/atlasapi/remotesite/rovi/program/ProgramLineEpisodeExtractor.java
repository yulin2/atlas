package org.atlasapi.remotesite.rovi.program;

import org.atlasapi.media.entity.Episode;
import org.atlasapi.remotesite.rovi.KeyedFileIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProgramLineEpisodeExtractor extends ProgramLineBaseExtractor<RoviProgramLine, Episode> {

    protected ProgramLineEpisodeExtractor(
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex) {
        super(descriptionIndex);
    }

    private final static Logger LOG = LoggerFactory.getLogger(ProgramLineEpisodeExtractor.class);
    
    @Override
    protected Episode createContent() {
        return new Episode();
    }

    @Override
    protected Episode addSpecificData(Episode content, RoviProgramLine programLine) {
        return content;
    }

}
