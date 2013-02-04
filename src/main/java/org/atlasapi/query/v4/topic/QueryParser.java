package org.atlasapi.query.v4.topic;

import javax.servlet.http.HttpServletRequest;


public interface QueryParser<T> {

    T queryFrom(HttpServletRequest request);

}
