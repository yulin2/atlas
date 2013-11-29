package org.atlasapi.remotesite.talktalk;

import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.remotesite.talktalk.vod.bindings.ChannelType;

import com.google.common.base.Optional;

/**
 * Interface for use with {@link TalkTalkClient} for processing
 * {@link ChannelTypes}'s.
 * 
 * @param <R>
 */
public interface TalkTalkChannelProcessor<R> {
    
    R process(ChannelType channel, Optional<ContentGroup> contentGroup) throws TalkTalkException;
    
}
