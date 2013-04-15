package org.atlasapi.output;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.output.annotation.OutputAnnotation;
import org.atlasapi.query.common.ActiveAnnotations;
import org.atlasapi.query.common.QueryContext;
import org.atlasapi.query.common.Resource;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * Contains state required during the output of a response.
 * Not thread-safe 
 */
public class OutputContext {

    public static OutputContext valueOf(QueryContext standard) {
        return new OutputContext(standard.getAnnotations(),
                standard.getApplicationConfiguration());
    }
    
    private final ActiveAnnotations annotations;
    private final ApplicationConfiguration applicationConfiguration;
    private final List<Resource> resources;

    public OutputContext(ActiveAnnotations activeAnnotations,
        ApplicationConfiguration applicationConfiguration) {
        this.annotations = checkNotNull(activeAnnotations);
        this.applicationConfiguration = checkNotNull(applicationConfiguration);
        this.resources = Lists.newLinkedList();
    }

    public final OutputContext startResource(Resource resource) {
        resources.add(resource);
        return this;
    }
    
    public final OutputContext endResource() {
        resources.remove(resources.size()-1);
        return this;
    }
    
    public <T> List<OutputAnnotation<? super T>> getAnnotations(AnnotationRegistry<T> registry) {
        ImmutableSet<Annotation> active = annotations.forPath(resources);
        if (active == null || active.isEmpty()) {
            return registry.defaultAnnotations();
        }
        return registry.activeAnnotations(active);
    }
    
    public ApplicationConfiguration getApplicationConfiguration() {
        return this.applicationConfiguration;
    }

}
