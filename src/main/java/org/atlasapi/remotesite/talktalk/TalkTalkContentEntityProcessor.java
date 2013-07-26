package org.atlasapi.remotesite.talktalk;

import org.atlasapi.media.entity.Content;
import org.atlasapi.remotesite.talktalk.vod.bindings.VODEntityType;

/**
 * Processes TalkTalk {@link VODEntityType}s for {@link Content} types. 
 * @param <R>
 */
public interface TalkTalkContentEntityProcessor<R> {
    
    R processBrandEntity(VODEntityType entity);
    
    R processSeriesEntity(VODEntityType entity);
    
    R processEpisodeEntity(VODEntityType entity);

}
