package org.atlasapi.query.common;

import javax.servlet.http.HttpServletRequest;

public interface QueryParser<T> {

    Query<T> parse(HttpServletRequest request);
    
}
