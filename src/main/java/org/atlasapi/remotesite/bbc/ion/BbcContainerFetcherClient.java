package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Series;

import com.metabroadcast.common.base.Maybe;

public interface BbcContainerFetcherClient {

    Maybe<Brand> createBrand(String brandId);

    Maybe<Series> createSeries(String seriesId);

}