package org.atlasapi.output.simple;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.EntityType;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.simple.ContentIdentifier;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.entity.simple.Playlist;

public class ContainerModelSimplifier extends ContentModelSimplifier<Container, Playlist> {

    private final ItemModelSimplifier itemSimplifier = new ItemModelSimplifier();
    
    @Override
    public Playlist apply(Container fullPlayList) {

        Playlist simplePlaylist = new Playlist();
        simplePlaylist.setType(EntityType.from(fullPlayList).toString());

        copyBasicContentAttributes(fullPlayList, simplePlaylist);

        if (fullPlayList instanceof Series) {
            Series series = (Series) fullPlayList;
            simplePlaylist.setSeriesNumber(series.getSeriesNumber());
            simplePlaylist.setTotalEpisodes(series.getTotalEpisodes());
        }

        for (ChildRef child : fullPlayList.getChildRefs()) {
            simplePlaylist.add(ContentIdentifier.identifierFor(child));
        }
        return simplePlaylist;

    }

    @Override
    protected Item simplify(org.atlasapi.media.entity.Item item) {
        return itemSimplifier.simplify(item);
    }

}
