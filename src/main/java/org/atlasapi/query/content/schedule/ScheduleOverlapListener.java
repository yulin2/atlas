package org.atlasapi.query.content.schedule;

import org.atlasapi.media.content.Broadcast;
import org.atlasapi.media.content.Item;

public interface ScheduleOverlapListener {
    
    void itemRemovedFromSchedule(Item item, Broadcast broadcast);
    
}
