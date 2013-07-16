package org.atlasapi.remotesite.talktalk;

import org.atlasapi.remotesite.talktalk.vod.bindings.ChannelType;

/**
 * Interface for processing data from TalkTalk's TVStructure resource.
 * 
 * Process methods for other types can be added as required.
 *
 * @param <R>
 */
public interface TalkTalkTvStructureProcessor<R> extends TalkTalkProcessor<R> {
    
    void process(ChannelType channel);
    
}
