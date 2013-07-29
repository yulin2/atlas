package org.atlasapi.remotesite.talktalk;

import org.atlasapi.remotesite.talktalk.vod.bindings.VODEntityType;

/**
 * Processes TalkTalk {@link VODEntityType}s into some type R. 
 * @param <R>
 */
public interface TalkTalkVodEntityProcessor<R> {
    
    R processEntity(VODEntityType entity);

}
