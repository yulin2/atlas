package org.atlasapi.remotesite.redux;

import org.atlasapi.remotesite.redux.model.FullReduxProgramme;

public interface ReduxProgrammeHandler {

    void handle(FullReduxProgramme programme);
    
}
