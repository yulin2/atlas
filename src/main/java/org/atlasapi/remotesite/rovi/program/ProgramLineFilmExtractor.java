package org.atlasapi.remotesite.rovi.program;

import org.atlasapi.media.entity.Film;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.KeyedFileIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Extracts a {@link Film} from a {@link RoviProgramLine} with {@link RoviShowType} MO (Movie) 
 */
public class ProgramLineFilmExtractor extends ProgramLineBaseExtractor<RoviProgramLine, Film> {
    
    public ProgramLineFilmExtractor(
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex,
            ContentResolver contentResolver) {
        super(descriptionIndex, contentResolver);
    }

    private final static Logger LOG = LoggerFactory.getLogger(ProgramLineFilmExtractor.class);
    
    @Override
    protected Film createContent() {
        return new Film();
    }

    @Override
    protected Film addSpecificData(Film content, RoviProgramLine programLine) {
        return content;
    }
    
    @Override
    protected Logger log() {
        return LOG;
    }

}
