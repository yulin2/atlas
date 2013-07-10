package org.atlasapi.remotesite.thesun;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ResolvedContent;

import com.google.common.collect.ImmutableList;


public class TheSunTvPicksContentGroupUpdater {
    private final ContentGroupResolver contentGroupResolver;
    private final ContentGroupWriter contentGroupWriter;
    private final String contentGroupUri; 
    
    public TheSunTvPicksContentGroupUpdater(ContentGroupResolver contentGroupResolver,
            ContentGroupWriter contentGroupWriter, String contentGroupUri) {
        super();
        this.contentGroupResolver = contentGroupResolver;
        this.contentGroupWriter = contentGroupWriter;
        this.contentGroupUri = contentGroupUri;
    }

    public ContentGroup createOrRetrieveGroup() {
        ContentGroup contentGroup;
        ResolvedContent resolvedContent = contentGroupResolver.findByCanonicalUris(ImmutableList.of(contentGroupUri));
        if (resolvedContent.get(contentGroupUri).hasValue()) {
            contentGroup = (ContentGroup) resolvedContent.get(contentGroupUri).requireValue();
            contentGroup.setContents(ImmutableList.<ChildRef>of());
        } else {
            contentGroup = new ContentGroup(contentGroupUri, Publisher.THE_SUN);
        }
        return contentGroup;
    };
    
    public void saveGroup(ContentGroup contentGroup) {
        contentGroupWriter.createOrUpdate(contentGroup);
    }

}
