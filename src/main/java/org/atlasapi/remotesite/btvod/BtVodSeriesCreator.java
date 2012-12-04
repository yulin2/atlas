package org.atlasapi.remotesite.btvod;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.remotesite.btvod.model.BtVodItemData;

public class BtVodSeriesCreator implements BtVodContentCreator<Series> {

    @Override
    public Series extract(BtVodItemData data) {
        Series series = new Series();
        
        series.setCanonicalUri(data.getContainer().get());
        series.setTitle(data.getContainerTitle().get());
        series.withSeriesNumber(data.getSeriesNumber().get());
        series.addAlias(data.getContainerSelfLink().get());
        series.addAlias(data.getContainerExternalId().get());
        series.setSpecialization(Specialization.TV);
        series.setPublisher(Publisher.BT);
        
        return series;
    }

}
