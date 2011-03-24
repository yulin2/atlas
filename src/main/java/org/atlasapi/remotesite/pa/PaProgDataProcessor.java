package org.atlasapi.remotesite.pa;

import org.atlasapi.media.entity.Channel;
import org.atlasapi.remotesite.pa.bindings.ProgData;
import org.joda.time.DateTimeZone;

public interface PaProgDataProcessor {

    public void process(ProgData progData, Channel channel, DateTimeZone zone);

}