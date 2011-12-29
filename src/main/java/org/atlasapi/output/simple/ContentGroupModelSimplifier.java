package org.atlasapi.output.simple;

import java.util.List;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.simple.ContentIdentifier;
import org.atlasapi.media.entity.simple.Playlist;

import com.google.common.collect.Lists;

public class ContentGroupModelSimplifier extends DescribedModelSimplifier<ContentGroup, Playlist> {

    @Override
    public Playlist apply(ContentGroup fullPlayList) {
        
        Playlist simplePlaylist = new Playlist();
        
        copyBasicDescribedAttributes(fullPlayList, simplePlaylist);
        
        simplePlaylist.setContent(simpleContentListFrom(fullPlayList.getContents()));
        
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
