package org.atlasapi.remotesite.talktalk;

import org.atlasapi.remotesite.talktalk.vod.bindings.ChannelType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
import org.atlasapi.remotesite.talktalk.vod.bindings.VODEntityType;

/**
 * General interface for retrieving data from TalkTalk.
 */
public interface TalkTalkClient {
    
    /**
     * Interface for processing data from TalkTalk's TVStructure resource at
     * /TVDataInterface/TVStructure/Structure/1
     * 
     * For each ChannelType element in the response, process(ChannelType)
     * will be called once. This call is made during parsing of the document.
     * 
     * @param <R>
     */
    /*
     * Currently we're only interested in ChannelTypes so there's only a process
     * method for channels. If/when other types such as Guides or Genres are to be
     * processed then methods for that can be added.
     */
    public interface TalkTalkTvStructureCallback<R>  {

        R getResult();
        
        void process(ChannelType channel);
        
    }
    
    /**
     * Callback for use with {@link TalkTalkClient} processVodList for
     * processing {@link VODEntityType}'s in the result of
     * TVDataInterface/VOD/List/2
     * 
     * For each VODEntityType element in the response, process(VODEntityType)
     * will be called once. This call is made during parsing of the document.
     * 
     * @param <R>
     */
    public interface TalkTalkVodListCallback<R> {

        R getResult();
        
        void process(VODEntityType entity);
        
    }
    
    /**
     * Fetch and process the TvStructure using the given callback.
     * 
     * @param callback
     * @return the result of processing the structure according to the
     *         callback.
     * @throws TalkTalkException
     */
    <R> R processTvStructure(TalkTalkTvStructureCallback<R> callback)
            throws TalkTalkException;
    
    /**
     * Fetch and process the list of VOD entities for the provided type and
     * identifier using the given callback.
     * 
     * @param type
     * @param identifier
     * @param callback
     * @return the result of processing the list of entities according to the
     *         callback
     * @throws TalkTalkException
     */
    <R> R processVodList(GroupType type, String identifier,
            TalkTalkVodListCallback<R> callback)
            throws TalkTalkException;
    
    /**
     * Fetch the detail for the provided type and identifier.
     * 
     * @param type
     * @param identifier
     * @return
     * @throws TalkTalkException
     */
    ItemDetailType getItemDetail(GroupType type, String identifier)
            throws TalkTalkException;
    

}