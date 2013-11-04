package org.atlasapi.application;

import org.atlasapi.application.SourceStatus.SourceState;
import org.atlasapi.application.users.Role;
import org.atlasapi.application.users.User;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.NotFoundException;
import org.atlasapi.output.ResourceForbiddenException;
import org.atlasapi.persistence.application.ApplicationStore;
import org.atlasapi.persistence.application.SourceRequestStore;
import org.elasticsearch.common.Preconditions;

import com.google.common.base.Optional;
import com.metabroadcast.common.ids.IdGenerator;


public class SourceRequestManager {
    private final SourceRequestStore sourceRequestStore;
    private final ApplicationStore applicationStore;
    private final IdGenerator idGenerator;
    
    public SourceRequestManager(SourceRequestStore sourceRequestStore,
            ApplicationStore applicationStore,
            IdGenerator idGenerator) {
        this.sourceRequestStore = sourceRequestStore;
        this.applicationStore = applicationStore;
        this.idGenerator = idGenerator;
    }
    
    public SourceRequest createOrUpdateRequest(Publisher source, UsageType usageType,
            Id applicationId, String applicationUrl, String email, String reason) {
        Optional<SourceRequest> existing = sourceRequestStore.getBy(applicationId, source);
        Preconditions.checkNotNull(source);
        Preconditions.checkNotNull(applicationId);
        Preconditions.checkNotNull(usageType);
        if (existing.isPresent()) {
            return updateSourceRequest(existing.get(), usageType,
                    applicationUrl, email, reason);
        } else {
            return createSourceRequest(source, usageType,
                    applicationId, applicationUrl, email, reason);
        }
    }
    
    public SourceRequest createSourceRequest(Publisher source, UsageType usageType,
            Id applicationId, String applicationUrl, String email, String reason) {
        SourceRequest sourceRequest = SourceRequest.builder()
                .withId(Id.valueOf(idGenerator.generateRaw()))
                .withAppId(applicationId)
                .withAppUrl(applicationUrl)
                .withApproved(false)
                .withEmail(email)
                .withReason(reason)
                .withSource(source)
                .withUsageType(usageType)
                .build();
        sourceRequestStore.store(sourceRequest);
        return sourceRequest;
    }
    
    public SourceRequest updateSourceRequest(SourceRequest existing, UsageType usageType,
           String applicationUrl, String email, String reason) {
        SourceRequest sourceRequest = existing.copy()
                .withAppUrl(applicationUrl)
                .withEmail(email)
                .withReason(reason)
                .withUsageType(usageType)
                .build();
        sourceRequestStore.store(sourceRequest);
        return sourceRequest;
    }
    
    /**
     * Approve source request and change source status on app to available
     * Must be admin of source to approve
     * @param id
     * @throws NotFoundException
     * @throws ResourceForbiddenException 
     */
    public void approveSourceRequest(Id id, User approvingUser) throws NotFoundException, ResourceForbiddenException {
        Optional<SourceRequest> sourceRequest = sourceRequestStore.sourceRequestFor(id);
        if (!sourceRequest.isPresent()) {
            throw new NotFoundException(id);
        }
        if (!approvingUser.is(Role.ADMIN) 
                && !approvingUser.getSources().contains(sourceRequest.get().getSource())) {
            throw new ResourceForbiddenException();
        }
        Application existing = applicationStore.applicationFor(sourceRequest.get().getAppId()).get();
        applicationStore.updateApplication(
                    existing.copyWithReadSourceState(sourceRequest.get().getSource(), SourceState.AVAILABLE));
        SourceRequest approved = sourceRequest.get().copy().withApproved(true).build();
        sourceRequestStore.store(approved);
    }
}
