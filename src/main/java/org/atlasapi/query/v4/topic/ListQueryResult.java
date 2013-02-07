package org.atlasapi.query.v4.topic;
//
//import org.atlasapi.application.ApplicationConfiguration;
//import org.atlasapi.output.Annotation;
//
//import com.google.common.collect.FluentIterable;
//
//public class ListQueryResult<T> extends QueryResult {
//
//    private final FluentIterable<T> resources;
//
//    public ListQueryResult(Iterable<T> resources, Iterable<Annotation> annotations,
//        ApplicationConfiguration appConfig) {
//        super(annotations, appConfig);
//        this.resources = FluentIterable.from(resources);
//    }
//
//    public FluentIterable<T> getResources() {
//        return resources;
//    }
//
//}
