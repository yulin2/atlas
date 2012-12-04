package org.atlasapi.query.v4.schedule;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.output.Annotation;
import org.atlasapi.output.AnnotationRegistry.AnnotationSet;
import org.atlasapi.output.annotation.OutputAnnotation;

public class OutputContext {

    private final AnnotationSet annotations;
    private final ApplicationConfiguration applicationConfiguration;

    public OutputContext(AnnotationSet annotationSet,
        ApplicationConfiguration applicationConfiguration) {
        this.annotations = checkNotNull(annotationSet);
        this.applicationConfiguration = checkNotNull(applicationConfiguration);
    }

    public <T> List<OutputAnnotation<? super T>> getAnnotations(Class<? extends T> cls) {
        return this.annotations.map(cls, null);
    }

    public <T> List<OutputAnnotation<? super T>> getAnnotations(Class<? extends T> cls, Annotation dflt) {
        return this.annotations.map(cls, dflt);
    }

    public ApplicationConfiguration getApplicationConfiguration() {
        return this.applicationConfiguration;
    }

}
