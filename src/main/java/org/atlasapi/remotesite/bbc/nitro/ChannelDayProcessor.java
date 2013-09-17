package org.atlasapi.remotesite.bbc.nitro;

import org.atlasapi.media.channel.Channel;
import org.joda.time.LocalDate;

import com.metabroadcast.common.scheduling.UpdateProgress;

/**
 * Performs an action for the given {@link Channel} and {@link LocalDate} day.
 */
public interface ChannelDayProcessor {

    UpdateProgress process(Channel channel, LocalDate date) throws Exception;

}
