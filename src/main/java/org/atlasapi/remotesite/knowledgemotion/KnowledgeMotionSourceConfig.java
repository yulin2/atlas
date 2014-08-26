package org.atlasapi.remotesite.knowledgemotion;

import org.atlasapi.media.entity.Publisher;

public abstract class KnowledgeMotionSourceConfig {
    abstract String rowHeader();
    abstract Publisher publisher();
    abstract String curie(String id);
    abstract String uri(String id);

    public static KnowledgeMotionSourceConfig from(final String rowHeader, final Publisher publisher, final String curiePattern, final String uriPattern) {
        return new KnowledgeMotionSourceConfig() {
            @Override
            public String uri(String id) {
                return String.format(uriPattern, id);
            }

            @Override
            public String rowHeader() {
                return rowHeader;
            }

            @Override
            public Publisher publisher() {
                return publisher;
            }

            @Override
            public String curie(String id) {
                return String.format(curiePattern, id);
            }
        };
    }
}
