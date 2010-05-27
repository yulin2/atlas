package org.uriplay.query.content;

import com.metabroadcast.common.base.Maybe;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public interface CurieExpander {

	Maybe<String> expand(String curie);

}
