package org.atlasapi.application;

import java.util.List;
import java.util.Map;

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
    
    public Application applicationFor(Id id) throws NotFoundException {
        Optional<Application> application = applicationStore.applicationFor(id);
        if (application.isPresent()) {
            return application.get();
        } else {
            throw new NotFoundException(id);
        }
    }
    
    public void storeApplication(Application application) {
        applicationStore.store(application);
    }
    
    public Application addIdAndApiKey(Application application) {
        Id id = Id.valueOf(idGenerator.generateRaw());
        String apiKey = adminHelper.generateApiKey() ;
        ApplicationCredentials credentials = application.getCredentials()
                .copy().withApiKey(apiKey).build();
        Application modified = application.copy()
                .withId(id)
                .withCredentials(credentials)
                .withSlug(adminHelper.generateSlug(id))
                .build();
        return modified;
    }

    public Application replaceSources(Application application, ApplicationSources sources) {
        return application.copy().withSources(sources).build();
    }

    public Application changeReadSourceState(Application application, 
            Publisher source, SourceState sourceState) {
        SourceStatus status = findSourceStatusFor(source, application.getSources().getReads());
        SourceStatus newStatus = status.copyWithState(sourceState);
        return modifyReadSourceStatus(application, source, newStatus);
    }

    public Application enableSource(Application application, Publisher source) {
        SourceStatus status = findSourceStatusFor(source, application.getSources().getReads());
        status = status.enable();
        return modifyReadSourceStatus(application, source, status);
    }
    
    public Application disableSource(Application application, Publisher source) {
        SourceStatus status = findSourceStatusFor(source, application.getSources().getReads());
        status = status.disable();
        return modifyReadSourceStatus(application, source, status);
    }
    
    private SourceStatus findSourceStatusFor(Publisher source, List<SourceReadEntry> reads) {
        for (SourceReadEntry status : reads) {
            if (status.getPublisher().equals(source)) {
                return status.getSourceStatus();
            }
        }
        return null;
    }

    private Application modifyReadSourceStatus(Application application, Publisher source,
            SourceStatus status) {
        List<SourceReadEntry> modifiedReads = changeReadsPreservingOrder(
                application.getSources().getReads(), source, status);
        ApplicationSources modifiedSources = application.getSources().copy()
                .withReads(modifiedReads).build();
        return application.copy().withSources(modifiedSources).build();
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
    
   

    public Application addWrites(Application application, Publisher source) {
        List<Publisher> writes = Lists.newArrayList(application.getSources().getWrites());
        if (!writes.contains(source)) {
            writes.add(source);
        }
        ApplicationSources modifiedSources = application
                    .getSources().copy().withWrites(writes).build();
        return application.copy().withSources(modifiedSources).build();
    }

    public Application removeWrites(Application application, Publisher source) {
        List<Publisher> writes = Lists.newArrayList(application.getSources().getWrites());
        writes.remove(source);
        ApplicationSources modifiedSources = application
                    .getSources().copy().withWrites(writes).build();
        return application.copy().withSources(modifiedSources).build();
    }
    
    public Application disablePrecendence(Application application) {
        ApplicationSources modifiedSources = application
               .getSources().copy().withPrecedence(false).build();
        return application.copy().withSources(modifiedSources).build();
    }

    public Application setPrecendenceOrder(Application application, List<Publisher> ordering) {
        Map<Publisher, SourceReadEntry> sourceMap = convertToKeyedMap(application
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
               System.out.println("Not seen: " + source);
            }
        }
        ApplicationSources modifiedSources = application
                    .getSources().copy()
                    .withPrecedence(true)
                    .withReads(readsWithNewOrder)
                    .build();
            
        return application.copy().withSources(modifiedSources).build();
    }
    
    private Map<Publisher, SourceReadEntry> convertToKeyedMap(List<SourceReadEntry> reads) {
        Builder<Publisher, SourceReadEntry> sourceMap =ImmutableMap.builder();
        for (SourceReadEntry read : reads) {
            sourceMap.put(read.getPublisher(), read);
        }
        return sourceMap.build();
    }
}
