package org.atlasapi.remotesite.talktalk;

import org.atlasapi.remotesite.talktalk.vod.bindings.VODEntityType;

/**
 * Interface for use with {@link TalkTalkClient} for processing
 * {@link VODEntityType}'s.
 * 
 * @param <R>
 */
public interface TalkTalkVodEntityProcessor<R> extends TalkTalkProcessor<R> {

    void process(VODEntityType entity);
    
}
