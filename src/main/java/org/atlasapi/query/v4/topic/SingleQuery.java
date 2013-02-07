//package org.atlasapi.query.v4.topic;
//
//import static com.google.common.base.Preconditions.checkNotNull;
//
//import org.atlasapi.application.ApplicationConfiguration;
//import org.atlasapi.media.common.Id;
//import org.atlasapi.output.Annotation;
//
//
//public class SingleQuery<T> extends AbstractQuery {
//
//    private final Id id;
//
//    public SingleQuery(Id id, ApplicationConfiguration appConfig, Iterable<Annotation> annotations) {
//        super(appConfig, annotations);
//        this.id = checkNotNull(id);
//    }
//
//    public Id getId() {
//        return id;
//    }
//
//}
