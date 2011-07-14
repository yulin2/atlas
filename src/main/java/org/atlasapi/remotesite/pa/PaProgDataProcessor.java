package org.atlasapi.remotesite.pa;

import org.atlasapi.media.entity.Channel;
import org.atlasapi.remotesite.pa.bindings.ProgData;
import org.joda.time.DateTimeZone;

import com.metabroadcast.common.time.Timestamp;

public interface PaProgDataProcessor {

    public void process(ProgData progData, Channel channel, DateTimeZone zone, Timestamp updatedAt);

}