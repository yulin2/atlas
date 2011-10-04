package org.atlasapi.remotesite.redux;

import java.util.List;

import org.atlasapi.remotesite.redux.model.BaseReduxProgramme;
import org.atlasapi.remotesite.redux.model.FullReduxProgramme;
import org.atlasapi.remotesite.redux.model.PaginatedBaseProgrammes;
import org.joda.time.LocalDate;

import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.query.Selection;

public interface ReduxClient {

    List<BaseReduxProgramme> programmesForDay(LocalDate date);
    
    Maybe<FullReduxProgramme> programmeFor(String diskRef);
    
    PaginatedBaseProgrammes latest(Selection selection);
    
}
