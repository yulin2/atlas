package org.atlasapi.application;

import java.util.Map;

import org.atlasapi.application.SourceStatus.SourceState;
import org.atlasapi.application.model.Application;
import org.atlasapi.application.model.ApplicationSources;
import org.atlasapi.application.persistence.ApplicationIdProvider;
import org.atlasapi.application.persistence.ApplicationStore;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.NotFoundException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
public class ApplicationUpdater {
    private final ApplicationStore applicationStore;
    private final ApplicationIdProvider idProvider;

    public ApplicationUpdater(ApplicationStore applicationStore, 
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

    public void updateSources(Id id, ApplicationSources sources) throws NotFoundException {
        Optional<Application> application = applicationStore.applicationFor(id);
        if (application.isPresent()) {
            Application modified = application.get().copy().withSources(sources).build();
            applicationStore.store(modified);
        } else {
            throw new NotFoundException(id);
        }
        
    }
    
    public void updateSourceState(Id id, Publisher source, SourceState sourceState) throws NotFoundException {
        Optional<Application> application = applicationStore.applicationFor(id);
        if (application.isPresent()) {
            SourceStatus status = application.get().getSources().getReads().get(source);
            status.copyWithState(sourceState);
            
            modifyReadPublisher(application.get(), source, status);
        } else {
            throw new NotFoundException(id);
        }
    }
    
    public void updateEnabled(Id id, Publisher source, boolean enabled) throws NotFoundException {
        Optional<Application> application = applicationStore.applicationFor(id);
        if (application.isPresent()) {
            SourceStatus status = application.get().getSources().getReads().get(source);
            if (enabled) {
                status = status.enable();
            } else {
                status = status.disable();
            }
            modifyReadPublisher(application.get(), source, status);
          
        } else {
            throw new NotFoundException(id);
        }
    }
    
    private void modifyReadPublisher(Application application, Publisher source, 
            SourceStatus status) {
        Map<Publisher, SourceStatus> modifiedReads = changeReadsPreservingOrder(
                application.getSources().getReads(), source, status);
        ApplicationSources modifiedSources = application.getSources().copy()
                .withReads(modifiedReads).build();
        Application modified = application.copy().withSources(modifiedSources).build();
        applicationStore.store(modified);
    }
    
    private Map<Publisher, SourceStatus> changeReadsPreservingOrder(Map<Publisher, SourceStatus> original, 
            Publisher sourceToChange, 
            SourceStatus newSourceStatus) {
        Builder<Publisher, SourceStatus> builder = ImmutableMap.builder();
        for (Publisher source : original.keySet()) {
            if (source.equals(sourceToChange)) {
                builder.put(source, newSourceStatus);
            } else {
                builder.put(source, original.get(source));
            }
        }
        
        return builder.build();
    }

}
