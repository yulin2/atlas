package org.atlasapi.query.v4.topic;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.output.Annotation;

import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;

public class TopicQueryResult {

    private final FluentIterable<Topic> resources;
    private final ImmutableSet<Annotation> annotations;
    private final ApplicationConfiguration appConfig;

    public TopicQueryResult(Iterable<Topic> resources, Iterable<Annotation> annotations,
        ApplicationConfiguration appConfig) {
        this.resources = FluentIterable.from(resources);
        this.annotations = ImmutableSet.copyOf(annotations);
        this.appConfig = checkNotNull(appConfig);
    }

    public FluentIterable<Topic> getResources() {
        return resources;
    }

    public ImmutableSet<Annotation> getAnnotations() {
        return annotations;
    }

    public ApplicationConfiguration getApplicationConfiguration() {
        return appConfig;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("resources", resources)
            .add("annotations", annotations)
            .toString();
    }
    
}
