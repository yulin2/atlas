package org.atlasapi.query.v4.schedule;

import java.util.Set;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.entity.ChannelSchedule;
import org.atlasapi.output.Annotation;

public class ScheduleQueryResult {

    private final ChannelSchedule channelSchedule;
    private final Set<Annotation> annotations;
    private final ApplicationConfiguration applicationConfiguration;

    public ScheduleQueryResult(
        ChannelSchedule channelSchedule,
        Set<Annotation> annotations,
        ApplicationConfiguration applicationConfiguration) {
        this.channelSchedule = channelSchedule;
        this.annotations = annotations;
        this.applicationConfiguration = applicationConfiguration;
    }

    public Set<Annotation> getAnnotations() {
        return annotations;
    }

    public ChannelSchedule getChannelSchedule() {
        return channelSchedule;
    }

    public ApplicationConfiguration getApplicationConfiguration() {
        return applicationConfiguration;
    }

}
