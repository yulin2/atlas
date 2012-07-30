package org.atlasapi.remotesite.pa.persistence;

import org.atlasapi.media.channel.Channel;
import org.joda.time.LocalDate;

import com.google.common.base.Optional;

public interface PaScheduleVersionStore {

    public void store(Channel channel, LocalDate scheduleDay, long version);
    public Optional<Long> get(Channel channel, LocalDate scheduleDay);

}