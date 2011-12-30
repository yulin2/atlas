package org.atlasapi.output.simple;

import java.util.Set;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.EntityType;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.simple.ContentIdentifier;
import org.atlasapi.media.entity.simple.Item;
import org.atlasapi.media.entity.simple.Playlist;
import org.atlasapi.output.Annotation;

public class ContainerModelSimplifier extends ContentModelSimplifier<Container, Playlist> {

    private final ItemModelSimplifier itemSimplifier = new ItemModelSimplifier();

    @Override
    public Playlist simplify(Container fullPlayList, Set<Annotation> annotations) {

        Playlist simplePlaylist = new Playlist();

        copyBasicContentAttributes(fullPlayList, simplePlaylist, annotations);
        simplePlaylist.setType(EntityType.from(fullPlayList).toString());

        if (annotations.contains(Annotation.EXTENDED_DESCRIPTION)) {
            if (fullPlayList instanceof Series) {
                Series series = (Series) fullPlayList;
                simplePlaylist.setSeriesNumber(series.getSeriesNumber());
                simplePlaylist.setTotalEpisodes(series.getTotalEpisodes());
            }
        }

        if (annotations.contains(Annotation.SUB_ITEMS)) {
            for (ChildRef child : fullPlayList.getChildRefs()) {
                simplePlaylist.add(ContentIdentifier.identifierFor(child));
            }
        }

        return simplePlaylist;
    }

    @Override
    protected Item simplify(org.atlasapi.media.entity.Item item, Set<Annotation> annotations) {
        return itemSimplifier.simplify(item, annotations);
    }

}
