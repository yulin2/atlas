package org.uriplay.query.content;

import java.util.Set;

import org.uriplay.media.entity.Description;

public interface UnknownTypeQueryExecutor {

	Set<Description> executeQuery(String uri);

}
