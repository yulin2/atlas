package org.atlasapi.remotesite.redux.model;

import java.util.List;

public class PaginatedBaseProgrammes {

    private int first;
    private int last;
    private List<BaseReduxProgramme> results;
    
    public int getFirst() {
        return first;
    }

    public int getLast() {
        return last;
    }

    public List<BaseReduxProgramme> getResults() {
        return results;
    }
    
}
