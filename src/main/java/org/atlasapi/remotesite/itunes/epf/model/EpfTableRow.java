package org.atlasapi.remotesite.itunes.epf.model;

import java.util.List;

public abstract class EpfTableRow {

    private final List<String> rowParts;

    public EpfTableRow(List<String> rowParts) {
        this.rowParts = rowParts;
    }
    
    public <R, S extends EpfTableColumn<R>> R get(S col) {
        return col.getValue(rowParts);
    }
    
}
