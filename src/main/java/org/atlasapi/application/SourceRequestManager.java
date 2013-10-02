package org.atlasapi.application;

import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.application.SourceRequestStore;
import org.elasticsearch.common.Preconditions;

import com.google.common.base.Optional;
import com.metabroadcast.common.ids.IdGenerator;


public class SourceRequestManager {
    private final SourceRequestStore sourceRequestStore;
    private final IdGenerator idGenerator;
    
    public SourceRequestManager(SourceRequestStore sourceRequestStore,
            IdGenerator idGenerator) {
        this.sourceRequestStore = sourceRequestStore;
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
}
