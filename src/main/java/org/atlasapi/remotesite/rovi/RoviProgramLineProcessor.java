package org.atlasapi.remotesite.rovi;

import java.nio.charset.Charset;

import org.atlasapi.media.entity.Content;
import org.atlasapi.remotesite.rovi.program.ProgramLineContentExtractorSupplier;
import org.atlasapi.remotesite.rovi.program.RoviProgramLine;
import org.atlasapi.remotesite.rovi.program.RoviProgramLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

public class RoviProgramLineProcessor extends RoviLineExtractorAndWriter<RoviProgramLine> {
    
    private static final Logger LOG = LoggerFactory.getLogger(RoviProgramLineProcessor.class);

    private final ProgramLineContentExtractorSupplier contentExtractorSupplier;
    private final Predicate<RoviProgramLine> isToProcess;
    
    public RoviProgramLineProcessor(RoviProgramLineParser programLineParser,
            ProgramLineContentExtractorSupplier contentExtractorSupplier, Predicate<RoviProgramLine> isToProcess, RoviContentWriter contentWriter, Charset charset) {
        
        super(programLineParser, charset, contentWriter);
        this.contentExtractorSupplier = contentExtractorSupplier;
        this.isToProcess = isToProcess;
    }

    @Override
    protected Logger log() {
        return LOG;
    }

    @Override
    protected Content extractContent(RoviProgramLine programLine) throws IndexAccessException {
        RoviContentExtractor<RoviProgramLine, ? extends Content> contentExtractor = contentExtractorSupplier.getContentExtractor(programLine.getShowType());
        return contentExtractor.extract(programLine);
    }

    @Override
    protected boolean isToProcess(RoviProgramLine parsedLine) {
        return isToProcess.apply(parsedLine);
    }

    @Override
    protected void handleProcessingException(Exception e, String line) {
        // Swallow the exception and logs
        log().error(errorMessage(line), e);
    }

}