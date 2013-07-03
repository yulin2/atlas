package org.atlasapi.remotesite.talktalk;

import org.atlasapi.remotesite.talktalk.vod.bindings.ChannelType;

/**
 * Interface for use with {@link TalkTalkClient} for processing
 * {@link ChannelTypes}'s.
 * 
 * @param <R>
 */
public interface TalkTalkChannelProcessor<R> {
    
    R process(ChannelType channel) throws TalkTalkException;
    
}
