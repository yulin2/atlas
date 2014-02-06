package org.atlasapi.remotesite.rovi.series;

import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Series;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.rovi.RoviUtils;


public class SeriesFromSeasonHistoryExtractor implements ContentExtractor<RoviSeasonHistoryLine, Series> {

    @Override
    public Series extract(RoviSeasonHistoryLine season) {
        Series series = new Series();
        
        series.setCanonicalUri(RoviUtils.canonicalUriForSeason(season.getSeasonProgramId()));
        
        String brandCanonicalUri = RoviUtils.canonicalUriForProgram(season.getSeriesId());
        series.setParentRef(new ParentRef(brandCanonicalUri));
        
        if (season.getSeasonName().isPresent()) {
            series.setTitle(season.getSeasonName().get());
        }
        
        if (season.getSeasonNumber().isPresent()) {
            series.withSeriesNumber(season.getSeasonNumber().get());
        }
        
        return series;
    }

}
