//package org.atlasapi.query.v4.topic;
//
//import static com.google.common.base.Preconditions.checkNotNull;
//
//import org.atlasapi.application.ApplicationConfiguration;
//import org.atlasapi.output.Annotation;
//
//public class SingleQueryResult<T> extends QueryResult {
//
//    private final T resource;
//
//    public SingleQueryResult(T resource, Iterable<Annotation> annotations,
//        ApplicationConfiguration appConfig) {
//        super(annotations, appConfig);
//        this.resource = checkNotNull(resource);
//    }
//
//    public T getResource() {
//        return resource;
//    }
//
//}
