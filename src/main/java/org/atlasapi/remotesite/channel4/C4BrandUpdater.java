package org.atlasapi.remotesite.channel4;

public interface C4BrandUpdater {
	
	void createOrUpdateBrand(String uri);

	boolean canFetch(String uri);

}
