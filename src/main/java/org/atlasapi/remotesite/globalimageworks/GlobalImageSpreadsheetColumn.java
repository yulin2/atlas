package org.atlasapi.remotesite.globalimageworks;

import static com.google.common.base.Preconditions.checkNotNull;

public enum GlobalImageSpreadsheetColumn {

    SOURCE("source"),
    ID("namespace"),
    TITLE("Title"),
    DESCRIPTION("Description"),
    DATE("Date"),
    DURATION("Duration"),
    KEYWORDS("Keywords")
    ;
    
    private final String value;
    
    private GlobalImageSpreadsheetColumn(String value) {
        this.value = checkNotNull(value);
    }
    
    public String getValue() {
        return value;
    }
    
}
