package org.atlasapi.remotesite.talktalk;

import org.atlasapi.remotesite.talktalk.vod.bindings.ChannelType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
import org.atlasapi.remotesite.talktalk.vod.bindings.VODEntityType;


public class StubTalkTalkClient implements TalkTalkClient {
    
    @Override
    public <R> R processTvStructure(TalkTalkTvStructureProcessor<R> processor)
            throws TalkTalkException {
        ChannelType channel = new ChannelType();
        channel.setId("channelId");
        processor.process(channel);
        return processor.getResult();
    }
    
    @Override
    public <R> R processVodList(GroupType type, String identifier,
            TalkTalkVodListProcessor<R> processor) throws TalkTalkException {
        VODEntityType entity = new VODEntityType();
        entity.setId(identifier);
        entity.setItemType(type.getItemType().get());
        processor.process(entity);
        return processor.getResult();
    }
    
    @Override
    public ItemDetailType getItemDetail(GroupType type, String identifier)
            throws TalkTalkException {
        ItemDetailType detail = new ItemDetailType();
        detail.setId(identifier);
        detail.setItemType(type.getItemType().get());
        return detail;
    }

}
