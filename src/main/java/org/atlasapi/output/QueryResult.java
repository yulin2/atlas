package org.atlasapi.output;

import javax.annotation.Nullable;

import org.atlasapi.media.entity.Identified;

import com.google.common.base.Optional;


public class QueryResult<CONTENT, CONTEXT> {
    
    public static <CONTENT extends Identified, CONTEXT extends Identified> QueryResult<CONTENT, CONTEXT> of(Iterable<CONTENT> content) {
        return of(content, null);
    }

    public static <CONTENT extends Identified, CONTEXT extends Identified> QueryResult<CONTENT, CONTEXT> of(Iterable<CONTENT> content, @Nullable CONTEXT context) {
        return new QueryResult<CONTENT, CONTEXT>(content, Optional.fromNullable(context));
    }

    private final Iterable<CONTENT> content;
    private final Optional<CONTEXT> context;

    QueryResult(Iterable<CONTENT> content, Optional<CONTEXT> context) {
        this.content = content;
        this.context = context;
    }

    public Optional<CONTEXT> getContext() {
        return context;
    }

    public Iterable<CONTENT> getContent() {
        return content;
    }
}
