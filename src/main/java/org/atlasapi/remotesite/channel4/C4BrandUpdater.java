package org.atlasapi.remotesite.channel4;

import org.atlasapi.media.content.Brand;

public interface C4BrandUpdater {
	
	Brand createOrUpdateBrand(String uri);

	boolean canFetch(String uri);

}
