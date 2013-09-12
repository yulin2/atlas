package org.atlasapi.application;

import java.util.List;
import org.atlasapi.application.SourceStatus.SourceState;
import org.atlasapi.application.model.Application;
import org.atlasapi.application.model.ApplicationSources;
import org.atlasapi.application.model.SourceReadEntry;
import org.atlasapi.application.persistence.ApplicationStore;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.NotFoundException;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.ids.IdGenerator;
import com.metabroadcast.common.ids.NumberToShortStringCodec;

public class ApplicationUpdater {

    private final ApplicationStore applicationStore;
    private final IdGenerator idGenerator;
    private final NumberToShortStringCodec idCodec;

    public ApplicationUpdater(ApplicationStore applicationStore,
            IdGenerator idGenerator,
            NumberToShortStringCodec idCodec) {
        this.applicationStore = applicationStore;
        this.idGenerator = idGenerator;
        this.idCodec = idCodec;
    }

    // For compatibility with 3.0
    private String getSlug(Application application) {
        if (application.getSlug() != null && !application.getSlug().isEmpty()) {
            return application.getSlug();
        } else {
            return "app-" + idCodec.encode(application.getId().toBigInteger());
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
            application = application.copy().withId(Id.valueOf(idGenerator.generateRaw())).build();
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

    public void updateSourceState(Id id, Publisher source, SourceState sourceState)
            throws NotFoundException {
        Optional<Application> application = applicationStore.applicationFor(id);
        if (application.isPresent()) {
            SourceStatus status = findSourceStatusFor(source, application.get().getSources().getReads());
            status.copyWithState(sourceState);

            modifyReadPublisher(application.get(), source, status);
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

}
