package org.atlasapi.query.v4.topic;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.output.Annotation;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;

public abstract class QueryResult<T> {

    private final ImmutableSet<Annotation> annotations;
    private final ApplicationConfiguration appConfig;

    protected QueryResult(Iterable<Annotation> annotations,
        ApplicationConfiguration appConfig) {
        this.annotations = ImmutableSet.copyOf(annotations);
        this.appConfig = checkNotNull(appConfig);
    }

    public ImmutableSet<Annotation> getAnnotations() {
        return annotations;
    }

    public ApplicationConfiguration getApplicationConfiguration() {
        return appConfig;
    }

    public abstract boolean isListResult();

    public abstract FluentIterable<T> getResources();

    public abstract T getOnlyResource();

    private final class SingleQueryResult extends QueryResult<T> {

        private final T resource;

        public SingleQueryResult(T resource, Iterable<Annotation> annotations,
            ApplicationConfiguration appConfig) {
            super(annotations, appConfig);
            this.resource = checkNotNull(resource);
        }

        @Override
        public boolean isListResult() {
            return false;
        }

        @Override
        public FluentIterable<T> getResources() {
            throw new IllegalStateException(
                "QueryResult.getResources() cannot be called on single result");
        }

        public T getOnlyResource() {
            return resource;
        }

    }

    private final class ListQueryResult extends QueryResult<T> {

        private final FluentIterable<T> resources;

        public ListQueryResult(Iterable<T> resources, Iterable<Annotation> annotations,
            ApplicationConfiguration appConfig) {
            super(annotations, appConfig);
            this.resources = FluentIterable.from(resources);
        }
        
        @Override
        public boolean isListResult() {
            return true;
        }

        public FluentIterable<T> getResources() {
            return resources;
        }

        public T getOnlyResource() {
            throw new IllegalStateException(
                "QueryResult.getOnlyResource() cannot be called on single result");
        }

    }

}
