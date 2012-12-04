package org.atlasapi.query.v4.schedule;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.entity.ChannelSchedule;
import org.atlasapi.output.Annotation;


public class ScheduleQueryResult {

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final ChannelSchedule channelSchedule;
    private final Set<Annotation> annotations;
    private final ApplicationConfiguration applicationConfiguration;

    public ScheduleQueryResult(HttpServletRequest request,
        HttpServletResponse response,
        ChannelSchedule channelSchedule,
        Set<Annotation> annotations,
        ApplicationConfiguration applicationConfiguration) {
            this.request = request;
            this.response = response;
            this.channelSchedule = channelSchedule;
            this.annotations = annotations;
            this.applicationConfiguration = applicationConfiguration;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
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
