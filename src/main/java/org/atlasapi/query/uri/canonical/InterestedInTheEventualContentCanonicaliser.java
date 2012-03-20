package org.atlasapi.query.uri.canonical;

import org.atlasapi.media.content.Identified;

public interface InterestedInTheEventualContentCanonicaliser extends Canonicaliser {

	void resolvedTo(String url, Identified resolvedTo);
	
}
