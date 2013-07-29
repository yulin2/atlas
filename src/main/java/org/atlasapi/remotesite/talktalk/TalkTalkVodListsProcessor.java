package org.atlasapi.remotesite.talktalk;

import java.util.List;

import org.atlasapi.media.entity.Content;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;

public interface TalkTalkVodListsProcessor<R> {
    
    R process(ContentGroupResolver resolver, ContentGroupWriter writer, List<Content> contentList, String groupId) throws TalkTalkException;

}
