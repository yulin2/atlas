package org.atlasapi.remotesite.talktalk;

import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemTypeType;

/**
 * General interface for retrieving data from TalkTalk.
 */
public interface TalkTalkClient {
    
    /**
     * Fetch and processor the TvStructure using the given processor.
     * 
     * @param processor
     * @return the result of processing the structure according to the
     *         processor.
     * @throws TalkTalkException
     */
    <R> R processTvStructure(TalkTalkTvStructureProcessor<R> processor)
            throws TalkTalkException;
    
    /**
     * Fetch and process the list of VOD entities for the provided type and
     * identifier using the given processor.
     * 
     * @param type
     * @param identifier
     * @param processor
     * @param itemsPerPage
     * @return the result of processing the list of entities according to the
     *         processor
     * @throws TalkTalkException
     */
    <R> R processVodList(ItemTypeType type, String identifier,
            TalkTalkVodEntityProcessor<R> processor, int itemsPerPage)
            throws TalkTalkException;
    
    /**
     * Fetch the detail for the provided type and identifier.
     * 
     * @param type
     * @param identifier
     * @return
     * @throws TalkTalkException
     */
    ItemDetailType getItemDetail(ItemTypeType type, String identifier)
            throws TalkTalkException;
    
}