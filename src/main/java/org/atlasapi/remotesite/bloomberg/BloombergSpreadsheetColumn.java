package org.atlasapi.remotesite.bloomberg;

import static com.google.common.base.Preconditions.checkNotNull;

public enum BloombergSpreadsheetColumn {

    SOURCE("source"),
    ID("namespace"),
    TITLE("Title"),
    DESCRIPTION("Description"),
    DATE("Date"),
    DURATION("Duration"),
    KEYWORDS("Keywords")
    ;
    
    private final String value;
    
    private BloombergSpreadsheetColumn(String value) {
        this.value = checkNotNull(value);
    }
    
    public String getValue() {
        return value;
    }
    
}
