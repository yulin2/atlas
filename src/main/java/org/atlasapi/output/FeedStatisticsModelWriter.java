package org.atlasapi.output;

import java.util.Set;

import org.atlasapi.application.v3.ApplicationConfiguration;
import org.atlasapi.feeds.youview.statistics.FeedStatistics;
import org.atlasapi.feeds.youview.statistics.FeedStatisticsQueryResult;

public class FeedStatisticsModelWriter extends TransformingModelWriter<Iterable<FeedStatistics>, FeedStatisticsQueryResult> {


    public FeedStatisticsModelWriter(AtlasModelWriter<FeedStatisticsQueryResult> delegate) {
        super(delegate);
    }
    
    @Override
    protected FeedStatisticsQueryResult transform(Iterable<FeedStatistics> feedStats, Set<Annotation> annotations, ApplicationConfiguration config) {
        FeedStatisticsQueryResult result = new FeedStatisticsQueryResult();
        for (FeedStatistics stats : feedStats) {
            result.add(stats);
        }
        return result;
    }

}
