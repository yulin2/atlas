package org.atlasapi.application.persistence;

import org.atlasapi.media.common.Id;


public interface ApplicationIdProvider {
    public Id issueNextId();
}
