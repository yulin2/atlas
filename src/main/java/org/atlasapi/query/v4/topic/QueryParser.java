package org.atlasapi.query.v4.topic;

import javax.servlet.http.HttpServletRequest;


public interface QueryParser<T> {

    Query<T> parse(HttpServletRequest request);
    
}
