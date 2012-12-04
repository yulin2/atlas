package org.atlasapi.remotesite.btvod;

import java.util.Set;

import nu.xom.Element;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Series;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.btvod.model.BtVodItemData;

import com.google.common.collect.ImmutableSet;

public class BtVodContentExtractor implements ContentExtractor<Element, Set<? extends Content>> {

    private final BtVodContentCreator<Film> filmCreator;
    private final BtVodContentCreator<Episode> episodeCreator;
    private final BtVodContentCreator<Series> seriesCreator;
    private final BtVodItemDataExtractor itemDataExtractor;
    
    public BtVodContentExtractor(BtVodContentCreator<Film> filmCreator, BtVodContentCreator<Episode> episodeCreator, 
            BtVodContentCreator<Series> seriesCreator, BtVodItemDataExtractor itemDataExtractor) {
        this.filmCreator = filmCreator;
        this.episodeCreator = episodeCreator;
        this.seriesCreator = seriesCreator;
        this.itemDataExtractor = itemDataExtractor;
    }
    
    @Override
    public Set<? extends Content> extract(Element source) {
        BtVodItemData data = itemDataExtractor.extract(source);
        
        if (isNotTopLevelItem(data)) {
            return ImmutableSet.of(episodeCreator.extract(data), seriesCreator.extract(data));            
        } else {
            return ImmutableSet.of(filmCreator.extract(data));
        }
    }

    // TODO not all of these fields are strictly needed, can drop some from here and check in the creators whether the field is present
    private boolean isNotTopLevelItem(BtVodItemData data) {
        return data.getContainer().isPresent() 
                && data.getContainerTitle().isPresent()
                && data.getContainerExternalId().isPresent()
                && data.getContainerSelfLink().isPresent()
                && data.getSeriesNumber().isPresent()
                && data.getEpisodeNumber().isPresent();
    }

}
