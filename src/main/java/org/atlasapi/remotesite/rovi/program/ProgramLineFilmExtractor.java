package org.atlasapi.remotesite.rovi.program;

import org.atlasapi.media.entity.Film;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProgramLineFilmExtractor extends ProgramLineBaseExtractor<RoviProgramLine, Film> {

    private final static Logger LOG = LoggerFactory.getLogger(ProgramLineFilmExtractor.class);
    
    @Override
    protected Film createContent() {
        return new Film();
    }

    @Override
    protected Film addSpecificData(Film content, RoviProgramLine programLine) {
        return content;
    }

}
