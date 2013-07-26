package org.atlasapi.remotesite.talktalk;

import java.util.List;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ResolvedContent;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.metabroadcast.common.scheduling.UpdateProgress;

public class ContentGroupUpdatingTalkTalkVodListsProcessor implements TalkTalkVodListsProcessor<UpdateProgress> {

    private final String TALK_TALK_URI_PART = "http://talktalk.net/groups/";

    @Override
    public UpdateProgress process(ContentGroupResolver resolver, ContentGroupWriter writer,
            List<Content> contentList, String groupId) throws TalkTalkException {

        String uri = TALK_TALK_URI_PART + groupId;

        ContentGroup contentGroup = resolveOrCreateContentGroup(resolver, uri);
        addChildRefs(contentList, contentGroup);
        writer.createOrUpdate(contentGroup);

        return UpdateProgress.SUCCESS;
    }

    private void addChildRefs(List<Content> contentList, ContentGroup contentGroup) {
        Builder<ChildRef> builder = ImmutableList.builder();
        for(Content content : contentList){
            builder.add(content.childRef());
        }
        contentGroup.setContents((builder.build()));
    }

    private ContentGroup resolveOrCreateContentGroup(ContentGroupResolver resolver, String uri) {
        ContentGroup contentGroup = null;
        ResolvedContent resolvedContent = resolver.findByCanonicalUris(ImmutableList.of(uri));
        if (resolvedContent.get(uri).hasValue()) {
            contentGroup = (ContentGroup) resolvedContent.get(uri).requireValue();
        } else {
            contentGroup = new ContentGroup(uri, Publisher.TALK_TALK);
        }
        return contentGroup;
    }

}
