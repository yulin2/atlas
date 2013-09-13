package org.atlasapi.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.atlasapi.application.SourceStatus.SourceState;
import org.atlasapi.application.model.Application;
import org.atlasapi.application.model.ApplicationCredentials;
import org.atlasapi.application.model.ApplicationSources;
import org.atlasapi.application.model.SourceReadEntry;
import org.atlasapi.application.persistence.ApplicationStore;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.NotFoundException;
import org.elasticsearch.common.collect.Lists;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.metabroadcast.common.ids.IdGenerator;

public class ApplicationUpdater {

    private final ApplicationStore applicationStore;
    private final IdGenerator idGenerator;
    private final AdminHelper adminHelper;

    public ApplicationUpdater(ApplicationStore applicationStore,
            IdGenerator idGenerator,
            AdminHelper adminHelper) {
        this.applicationStore = applicationStore;
        this.idGenerator = idGenerator;
        this.adminHelper = adminHelper;
    }

    public Application createOrUpdate(Application application) throws NotFoundException {
        Application modified = null;
        if (application.getId() != null) {
            Optional<Application> savedApplication = applicationStore.applicationFor(application.getId());
            if (savedApplication.isPresent()) {
                modified = updateApplication(application, savedApplication.get().getSlug());
            } else {
                throw new NotFoundException(application.getId());
            }
        } else {
            modified = createApplication(application);
        }
        applicationStore.store(modified);
        return application;
    }
    
    private Application createApplication(Application application) {
        Id id = Id.valueOf(idGenerator.generateRaw());
        String apiKey = generateApiKey() ;
        ApplicationCredentials credentials = application.getCredentials()
                .copy().withApiKey(apiKey).build();
        Application modified = application.copy()
                .withId(id)
                .withCredentials(credentials)
                .withSlug(adminHelper.generateSlug(id))
                .build();
        return modified;
    }
    
    private Application updateApplication(Application application, String slug) {
        if (slug == null) {
            slug = adminHelper.generateSlug(application.getId());
        }
        Application modified = application.copy()
                .withSlug(slug)
                .build();
        return modified;
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

    public void updateSourceState(Id id, Publisher source, SourceState sourceState)
            throws NotFoundException {
        Optional<Application> application = applicationStore.applicationFor(id);
        if (application.isPresent()) {
            SourceStatus status = findSourceStatusFor(source, application.get().getSources().getReads());
            SourceStatus newStatus = status.copyWithState(sourceState);
            modifyReadPublisher(application.get(), source, newStatus);
        } else {
            throw new NotFoundException(id);
        }
    }

    public void updateEnabled(Id id, Publisher source, boolean enabled) throws NotFoundException {
        Optional<Application> application = applicationStore.applicationFor(id);
        if (application.isPresent()) {
            SourceStatus status = findSourceStatusFor(source, application.get().getSources().getReads());
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
    
    private SourceStatus findSourceStatusFor(Publisher source, List<SourceReadEntry> reads) {
        for (SourceReadEntry status : reads) {
            if (status.getPublisher().equals(source)) {
                return status.getSourceStatus();
            }
        }
        return null;
    }

    private void modifyReadPublisher(Application application, Publisher source,
            SourceStatus status) {
        List<SourceReadEntry> modifiedReads = changeReadsPreservingOrder(
                application.getSources().getReads(), source, status);
        ApplicationSources modifiedSources = application.getSources().copy()
                .withReads(modifiedReads).build();
        Application modified = application.copy().withSources(modifiedSources).build();
        applicationStore.store(modified);
    }

    private List<SourceReadEntry> changeReadsPreservingOrder(
            List<SourceReadEntry> original,
            Publisher sourceToChange,
            SourceStatus newSourceStatus) {
        ImmutableList.Builder<SourceReadEntry> builder = ImmutableList.builder();
        for (SourceReadEntry source : original) {
            if (source.getPublisher().equals(sourceToChange)) {
                builder.add(new SourceReadEntry(source.getPublisher(), newSourceStatus));
            } else {
                builder.add(source);
            }
        }
        return builder.build();
    }
    
    public String generateApiKey() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public void addWrites(Id id, Publisher source) throws NotFoundException {
        Optional<Application> application = applicationStore.applicationFor(id);
        if (application.isPresent()) {
            List<Publisher> writes = Lists.newArrayList(application.get().getSources().getWrites());
            if (!writes.contains(source)) {
                writes.add(source);
            }
            ApplicationSources modifiedSources = application.get()
                    .getSources().copy().withWrites(writes).build();
            Application modified = application.get().copy().withSources(modifiedSources).build();
            applicationStore.store(modified);
        } else {
            throw new NotFoundException(id);
        }
    }

    public void removeWrites(Id id, Publisher source) throws NotFoundException {
        Optional<Application> application = applicationStore.applicationFor(id);
        if (application.isPresent()) {
            List<Publisher> writes = Lists.newArrayList(application.get().getSources().getWrites());
            writes.remove(source);
            ApplicationSources modifiedSources = application.get()
                    .getSources().copy().withWrites(writes).build();
            Application modified = application.get().copy().withSources(modifiedSources).build();
            applicationStore.store(modified);
        } else {
            throw new NotFoundException(id);
        }
    }
    
    public void disablePrecendence(Id id) throws NotFoundException {
        Optional<Application> application = applicationStore.applicationFor(id);
        if (application.isPresent()) {
            ApplicationSources modifiedSources = application.get()
                    .getSources().copy().withPrecedence(false).build();
            Application modified = application.get().copy().withSources(modifiedSources).build();
            applicationStore.store(modified);
        } else {
            throw new NotFoundException(id);
        }
    }

    public void setPrecendenceOrder(Id id, List<Publisher> ordering) throws NotFoundException {
        Optional<Application> application = applicationStore.applicationFor(id);
        if (application.isPresent()) {
            Map<Publisher, SourceReadEntry> sourceMap = convertToKeyedMap(application.get()
                .getSources().getReads());
            List<Publisher> seen = Lists.newArrayList();
            List<SourceReadEntry> readsWithNewOrder = Lists.newArrayList();
            for (Publisher source : ordering) {
                readsWithNewOrder.add(sourceMap.get(source));
                seen.add(source);
            }
            // add sources omitted from ordering
            for (Publisher source: sourceMap.keySet()) {
                if (!seen.contains(source)) {
                    readsWithNewOrder.add(sourceMap.get(source));
                }
            }
            
            ApplicationSources modifiedSources = application.get()
                    .getSources().copy()
                    .withPrecedence(true)
                    .withReads(readsWithNewOrder)
                    .build();
            
            Application modified = application.get().copy().withSources(modifiedSources).build();
            applicationStore.store(modified);
        } else {
            throw new NotFoundException(id);
        }
    }
    
    private Map<Publisher, SourceReadEntry> convertToKeyedMap(List<SourceReadEntry> reads) {
        Builder<Publisher, SourceReadEntry> sourceMap =ImmutableMap.builder();
        for (SourceReadEntry read : reads) {
            sourceMap.put(read.getPublisher(), read);
        }
        return sourceMap.build();
    }
}
