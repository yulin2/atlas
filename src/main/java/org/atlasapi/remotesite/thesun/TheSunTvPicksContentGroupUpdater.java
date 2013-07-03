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
    private static String CONTENT_GROUP_URI = "http://www.thesun.co.uk/sol/homepage/feeds/smartphone/newsection/";
    
    public TheSunTvPicksContentGroupUpdater(ContentGroupResolver contentGroupResolver,
            ContentGroupWriter contentGroupWriter) {
        super();
        this.contentGroupResolver = contentGroupResolver;
        this.contentGroupWriter = contentGroupWriter;
    }

    public ContentGroup createOrRetrieveGroup() {
        ContentGroup contentGroup;
        ResolvedContent resolvedContent = contentGroupResolver.findByCanonicalUris(ImmutableList.of(CONTENT_GROUP_URI));
        if (resolvedContent.get(CONTENT_GROUP_URI).hasValue()) {
            contentGroup = (ContentGroup) resolvedContent.get(CONTENT_GROUP_URI).requireValue();
            contentGroup.setContents(ImmutableList.<ChildRef>of());
        } else {
            contentGroup = new ContentGroup(CONTENT_GROUP_URI, Publisher.THE_SUN);
        }
        return contentGroup;
    };
    
    public void saveGroup(ContentGroup contentGroup) {
        contentGroupWriter.createOrUpdate(contentGroup);
    }

}
