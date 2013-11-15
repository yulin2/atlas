package org.atlasapi.remotesite.pa;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.remotesite.channel4.pmlsd.epg.ContentHierarchyAndBroadcast;
import org.atlasapi.remotesite.pa.listings.bindings.ProgData;
import org.joda.time.DateTimeZone;

import com.metabroadcast.common.time.Timestamp;

public interface PaProgDataProcessor {

    public ContentHierarchyAndBroadcast process(ProgData progData, Channel channel, DateTimeZone zone, Timestamp updatedAt);

}