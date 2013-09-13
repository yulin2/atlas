package org.atlasapi.application.persistence;

import org.atlasapi.application.model.Application;
import org.atlasapi.media.common.Id;

import com.google.common.base.Optional;

public interface ApplicationStore {

    Iterable<Application> allApplications();

    Optional<Application> applicationFor(Id id);

    void store(Application application);

    Iterable<Application> applicationsFor(Iterable<Id> ids);
}
