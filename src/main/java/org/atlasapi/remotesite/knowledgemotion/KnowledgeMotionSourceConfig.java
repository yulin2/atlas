package org.atlasapi.remotesite.knowledgemotion;

import org.atlasapi.media.entity.Publisher;

public interface KnowledgeMotionSourceConfig {
    String rowHeader();
    Publisher publisher();
    String curie(String id);
    String uri(String id);
}
