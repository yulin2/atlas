package org.atlasapi.output.simple;

import java.util.List;
import java.util.Set;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.simple.ContentIdentifier;
import org.atlasapi.media.entity.simple.Playlist;
import org.atlasapi.output.Annotation;

import com.google.common.collect.Lists;

public class ContentGroupModelSimplifier extends DescribedModelSimplifier<ContentGroup, Playlist> {

    @Override
    public Playlist simplify(ContentGroup model, Set<Annotation> annotations, ApplicationConfiguration config) {

        Playlist simplePlaylist = new Playlist();

        copyBasicDescribedAttributes(model, simplePlaylist, annotations);

        simplePlaylist.setContent(simpleContentListFrom(model.getContents()));

        return simplePlaylist;
    }

    private List<ContentIdentifier> simpleContentListFrom(Iterable<ChildRef> contents) {
        List<ContentIdentifier> contentList = Lists.newArrayList();
        for (ChildRef ref : contents) {
            contentList.add(ContentIdentifier.identifierFor(ref));
        }
        return contentList;
    }

}
