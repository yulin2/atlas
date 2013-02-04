package org.atlasapi.query.common;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.content.criteria.AtomicQuerySet;
import org.atlasapi.media.common.Id;

public abstract class Query<T> {

    public static final <T> Query<T> singleQuery(Id id, QueryContext context) {
        return new SingleQuery<T>(id, context);
    }

    public static final <T> Query<T> listQuery(AtomicQuerySet operands,
                                               QueryContext context) {
        return new ListQuery<T>(operands, context);
    }

    private final QueryContext context;

    protected Query(QueryContext context) {
        this.context = checkNotNull(context);
    }

    public QueryContext getContext() {
        return context;
    }

    public abstract boolean isListQuery();

    public abstract AtomicQuerySet getOperands();

    public abstract Id getOnlyId();

    private static final class SingleQuery<T> extends Query<T> {

        private final Id id;

        public SingleQuery(Id id, QueryContext context) {
            super(context);
            this.id = checkNotNull(id);
        }

        public Id getOnlyId() {
            return id;
        }

        @Override
        public boolean isListQuery() {
            return false;
        }

        @Override
        public AtomicQuerySet getOperands() {
            throw new IllegalStateException(
                "Query.getOperands() cannot be called on a single query");
        }

    }

    private static final class ListQuery<T> extends Query<T> {

        private final AtomicQuerySet operands;

        public ListQuery(AtomicQuerySet operands, QueryContext context) {
            super(context);
            this.operands = checkNotNull(operands);
        }

        public AtomicQuerySet getOperands() {
            return this.operands;
        }

        @Override
        public boolean isListQuery() {
            return true;
        }

        @Override
        public Id getOnlyId() {
            throw new IllegalStateException("Query.getOnlyId() cannot be called on a list query");
        }

    }

}
