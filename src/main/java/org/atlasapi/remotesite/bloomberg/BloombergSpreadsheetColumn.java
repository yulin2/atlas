package org.atlasapi.remotesite.bloomberg;

import static com.google.common.base.Preconditions.checkNotNull;

public enum BloombergSpreadsheetColumn {

    SOURCE("_cn6ca"),
    ID("embedcodereferenceid"),
    TITLE("name"),
    DESCRIPTION("description"),
    DATE("year"),
    DURATION("trt"),
    KEYWORDS("categories")
    ;
    
    private final String value;
    
    private BloombergSpreadsheetColumn(String value) {
        this.value = checkNotNull(value);
    }
    
    public String getValue() {
        return value;
    }
    
}
