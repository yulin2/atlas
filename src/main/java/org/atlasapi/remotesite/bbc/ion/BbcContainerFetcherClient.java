package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Series;
import org.atlasapi.remotesite.bbc.ion.model.IonContainer;

import com.metabroadcast.common.base.Maybe;

public interface BbcContainerFetcherClient {

    Maybe<Brand> createBrand(String brandId);

    Maybe<Series> createSeries(String seriesId);
    
    Maybe<IonContainer> getSubseries(String subseriesId);

}