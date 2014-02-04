package org.atlasapi.remotesite.rovi.program;

import org.atlasapi.media.entity.Brand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProgramLineBrandExtractor extends ProgramLineBaseExtractor<RoviProgramLine, Brand> {

    private final static Logger LOG = LoggerFactory.getLogger(ProgramLineBrandExtractor.class);
    
    @Override
    protected Brand createContent() {
        return new Brand();
    }

    @Override
    protected Brand addSpecificData(Brand content) {
        return content;
    }

}
