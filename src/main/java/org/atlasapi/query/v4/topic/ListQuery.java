//package org.atlasapi.query.v4.topic;
//
//import static com.google.common.base.Preconditions.checkNotNull;
//
//import org.atlasapi.application.ApplicationConfiguration;
//import org.atlasapi.content.criteria.AtomicQuerySet;
//import org.atlasapi.output.Annotation;
//
//import com.metabroadcast.common.query.Selection;
//
//public class ListQuery<T> extends AbstractQuery {
//
//    private final AtomicQuerySet operands;
//    private final Selection selection;
//
//    public ListQuery(AtomicQuerySet operands, Selection selection,
//        ApplicationConfiguration appConfig, Iterable<Annotation> annotations) {
//        super(appConfig, annotations);
//        this.operands = checkNotNull(operands);
//        this.selection = checkNotNull(selection);
//    }
//
//    public AtomicQuerySet getOperands() {
//        return this.operands;
//    }
//
//    public Selection getSelection() {
//        return this.selection;
//    }
//
//}
