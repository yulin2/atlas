package org.atlasapi.remotesite.knowledgemotion;

import org.atlasapi.media.entity.Content;

import com.google.common.base.Optional;

public interface KnowledgeMotionDataRowHandler {

    Optional<Content> handle(KnowledgeMotionDataRow row);
    void write(Content content);

}
