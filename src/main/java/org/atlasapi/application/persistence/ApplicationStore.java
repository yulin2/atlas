package org.atlasapi.application.persistence;

import org.atlasapi.application.model.Application;
import org.atlasapi.media.common.Id;

import com.google.common.base.Optional;


public interface ApplicationStore {
    public Iterable<Application> allApplications();
    public Optional<Application> applicationFor(Id id);

}
