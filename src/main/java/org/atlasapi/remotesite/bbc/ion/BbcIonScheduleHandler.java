package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;

public interface BbcIonScheduleHandler {

    /**
     * 
     * @param schedule
     * @return count of how many schedule items were handled
     */
    int handle(IonSchedule schedule);
    
}
