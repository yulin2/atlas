package org.atlasapi.application;

import org.atlasapi.application.model.Application;
import org.atlasapi.application.persistence.ApplicationIdProvider;
import org.atlasapi.application.persistence.ApplicationStore2;

import com.google.common.base.Optional;
public class ApplicationUpdater {
    private final ApplicationStore2 applicationStore;
    private final ApplicationIdProvider idProvider;

    public ApplicationUpdater(ApplicationStore2 applicationStore, 
            ApplicationIdProvider idProvider) {
        this.applicationStore = applicationStore;
        this.idProvider = idProvider;
    }
    
    // For compatibility with 3.0
    private String getSlug(Application application) {
        if (application.getSlug() != null && !application.getSlug().isEmpty()) {
            return application.getSlug();
        } else {
            return "app-" + String.valueOf(application.getId().longValue());
        }
    }
    
    public Application createOrUpdate(Application application) {
        if (application.getId() != null) {
            Optional<Application> savedApplication = applicationStore.applicationFor(application.getId());
            if (savedApplication.isPresent()) {
                application = application.copy().withSlug(savedApplication.get().getSlug()).build();
            }
        } else {
            // new application get an id
            application = application.copy().withId(idProvider.issueNextId()).build();
        }
        application = application.copy().withSlug(getSlug(application)).build();
        applicationStore.store(application);
        return application;
    }

}
