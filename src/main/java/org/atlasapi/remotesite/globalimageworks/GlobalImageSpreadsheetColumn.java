package org.atlasapi.remotesite.globalimageworks;

import static com.google.common.base.Preconditions.checkNotNull;

public enum GlobalImageSpreadsheetColumn {

    SOURCE("_cn6ca"),
    ID("embedcodereferenceid"),
    TITLE("name"),
    DESCRIPTION("description"),
    DATE("year"),
    DURATION("trt"),
    KEYWORDS("categories")
    ;
    
    private final String value;
    
    private GlobalImageSpreadsheetColumn(String value) {
        this.value = checkNotNull(value);
    }
    
    public String getValue() {
        return value;
    }
    
}
