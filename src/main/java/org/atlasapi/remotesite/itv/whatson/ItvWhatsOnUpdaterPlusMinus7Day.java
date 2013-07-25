package org.atlasapi.remotesite.itv.whatson;
import org.joda.time.DateTime;

import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.SystemClock;


public class ItvWhatsOnUpdaterPlusMinus7Day extends ScheduledTask {
    private Clock clock;
    private ItvWhatsOnUpdater itvUpdater;
    
    public ItvWhatsOnUpdaterPlusMinus7Day(ItvWhatsOnUpdater itvUpdater) {
        this(new SystemClock(), itvUpdater);
    }
    
    public ItvWhatsOnUpdaterPlusMinus7Day(Clock clock, ItvWhatsOnUpdater itvUpdater) {
       this.clock = clock;
       this.itvUpdater = itvUpdater;
    }

    @Override
    protected void runTask() {
        DateTime date = clock.now().minusDays(7);
        DateTime endDate = clock.now().plusDays(8);
        while (date.isBefore(endDate)) {
            itvUpdater.run(date);
            date = date.plusDays(1);
        }
    }

}
