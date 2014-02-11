package org.atlasapi.remotesite.rovi;

import java.nio.charset.Charset;

import org.atlasapi.media.entity.Content;


public abstract class RoviLineExtractorAndWriter<T extends KeyedLine<?>> extends RoviLineProcessor<T> {

    private final RoviContentWriter contentWriter;
    
    public RoviLineExtractorAndWriter(RoviLineParser<T> parser, Charset charset, RoviContentWriter contentWriter) {
        super(parser, charset);
        this.contentWriter = contentWriter;
    }
    
    protected abstract Content extractContent(T parsedLine) throws IndexAccessException;
    
    @Override
    protected void process(String line, T parsedLine) throws IndexAccessException {
        Content extractedContent = extractContent(parsedLine);
        contentWriter.writeContent(extractedContent);
    }
    
    @Override
    protected void doFinally(String line) {
        // Do nothing
    }
    
    @Override
    protected void handleBom(int bomLength) {
        // Do nothing
    }

}
