package org.atlasapi.remotesite.channel4.pmlsd;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.atlasapi.media.entity.Episode;
import org.atlasapi.query.content.PerPublisherCurieExpander;

import com.metabroadcast.common.time.Clock;
import com.sun.syndication.feed.atom.Entry;


public abstract class BaseC4EpisodeExtractor extends BaseC4ItemExtractor<Episode> {

    private static final String DC_EPISODE_NUMBER = "dc:relation.EpisodeNumber";
    private static final String DC_SERIES_NUMBER = "dc:relation.SeriesNumber";
    private static final String DC_PROGRAMME_ID = "dc:relation.programmeId";
    
    public BaseC4EpisodeExtractor(Clock clock) {
        super(clock);
    }

    @Override
    protected final Episode createItem(Entry entry, Map<String, String> lookup) {
        return new Episode();
    }

    @Override
    protected String getUri(Entry entry, Map<String, String> lookup) {
        String progId = lookup.get(DC_PROGRAMME_ID);
        checkNotNull(progId, "No programmeId in entry: %s", entry.getId());
        return C4AtomApi.PROGRAMMES_BASE + progId;
    }

    @Override
    protected final Episode setAdditionalItemFields(Entry entry, Map<String, String> lookup, Episode episode) {
        episode.setCurie(PerPublisherCurieExpander.CurieAlgorithm.C4.compact(episode.getCanonicalUri()));
        episode.setEpisodeNumber(C4AtomApi.readAsNumber(lookup, DC_EPISODE_NUMBER));
        episode.setSeriesNumber(C4AtomApi.readAsNumber(lookup, DC_SERIES_NUMBER));
        episode.setIsLongForm(true);
        return setAdditionalEpisodeFields(entry, lookup, episode);
    }

    protected abstract Episode setAdditionalEpisodeFields(Entry entry, Map<String, String> lookup, Episode episode);

}
