package org.atlasapi.remotesite.pa;

import org.atlasapi.remotesite.pa.bindings.ChannelData;
import org.atlasapi.remotesite.pa.bindings.ProgData;
import org.joda.time.DateTimeZone;

public interface PaProgDataProcessor {

    public void process(ProgData progData, ChannelData channelData, DateTimeZone zone);

}