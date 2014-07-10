package org.atlasapi.remotesite.btvod;

import java.io.IOException;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSource;
import com.google.common.io.LineProcessor;

public class BtVodData {

    private final Splitter splitter = Splitter.on("|").trimResults();
    
    public static final class BtVodDataRow {

        private final List<String> rowValues;
        private final List<String> columns;

        public BtVodDataRow(List<String> rowValues, List<String> columns) {
            this.rowValues = rowValues;
            this.columns = columns;
        }
        
        public String getColumnValue(BtVodFileColumn column) {
            int columnIndex = columns.indexOf(column.key());
            if (columnIndex < 0) {
                throw new RuntimeException("Column not found: " + column.key());
            }
            return rowValues.get(columnIndex);
        }
        
        @Override
        public String toString() {
            return rowValues.toString();
        }
        
    }

    private CharSource dataSupplier;
    
    public BtVodData(CharSource dataSupplier) {
        this.dataSupplier = dataSupplier;
    }
    
    public <T> T processData(BtVodDataProcessor<T> processor) throws IOException {
        return dataSupplier.readLines(lineProcessorWrap(processor));
    }
    
    private <T> LineProcessor<T> lineProcessorWrap(final BtVodDataProcessor<T> processor) {
        return new LineProcessor<T>() {

            List<String> headers;
            
            @Override
            public boolean processLine(String line) throws IOException {
                if (headers == null) {
                    headers = readHeaders(line);
                    return true;
                }
                return processor.process(rowFrom(line));
            }

            private BtVodDataRow rowFrom(String line) {
                List<String> values = ImmutableList.copyOf(splitter.split(line));
                return new BtVodDataRow(values, headers);
            }

            private List<String> readHeaders(String line) {
                return ImmutableList.copyOf(splitter.split(line));
            }

            @Override
            public T getResult() {
                return processor.getResult();
            }
            
        };
        
    }
}
