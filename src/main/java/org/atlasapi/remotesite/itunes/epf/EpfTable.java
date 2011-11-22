package org.atlasapi.remotesite.itunes.epf;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.atlasapi.remotesite.itunes.epf.model.EpfTableRow;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.LineProcessor;

public class EpfTable<ROW extends EpfTableRow> {

    private final Splitter splitter;
    private final InputSupplier<? extends Reader> inputSupplier;
    private final Function<List<String>, ROW> splitLineExtractor;
    
    public EpfTable(InputSupplier<? extends Reader> inputSupplier, Function<List<String>, ROW> splitLineExtractor) {
        this(inputSupplier, splitLineExtractor, String.valueOf((char) 1));
    }
    
    public EpfTable(InputSupplier<? extends Reader> inputSupplier, Function<List<String>, ROW> splitLineExtractor, String fieldSeparator) {
        this.inputSupplier = inputSupplier;
        this.splitLineExtractor = splitLineExtractor;
        this.splitter = Splitter.on(fieldSeparator);
    }
    
    public <RESULT> RESULT processRows(final EpfTableRowProcessor<ROW,RESULT> processor) throws IOException {
        return CharStreams.readLines(inputSupplier, lineProcessorForwardingTo(processor));
    }
    
    private <RESULT> LineProcessor<RESULT> lineProcessorForwardingTo(final EpfTableRowProcessor<ROW, RESULT> processor) {
        return new LineProcessor<RESULT>() {

            public boolean processLine(String line) throws IOException {
                if(isComment(line)) {
                    return true;
                }
                return processor.process(splitLineExtractor.apply(split(line)));
            }
            @Override
            public RESULT getResult() {
                return processor.getResult();
            }
        };
    }
    
    public List<String> split(String line) {
        return ImmutableList.copyOf(splitter.split(line.trim()));
    }
    
    private boolean isComment(String line) {
        return line.startsWith("#");
    }
}