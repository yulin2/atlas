package org.atlasapi.remotesite.talktalk;

import org.atlasapi.remotesite.talktalk.vod.bindings.ChannelType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemTypeType;
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
    public <R> R processVodList(ItemTypeType type, String identifier,
            TalkTalkVodEntityProcessor<R> processor, int itemsPerPage) throws TalkTalkException {
        VODEntityType entity = new VODEntityType();
        entity.setId(identifier);
        entity.setItemType(type);
        processor.process(entity);
        return processor.getResult();
    }
    
    @Override
    public ItemDetailType getItemDetail(ItemTypeType type, String identifier)
            throws TalkTalkException {
        ItemDetailType detail = new ItemDetailType();
        detail.setId(identifier);
        detail.setItemType(type);
        return detail;
    }

//    private Map<String, URL> respondsTo;
//
//    public StubTalkTalkClient(Map<String, URL> respondsTo) {
//        this.respondsTo = checkNotNull(respondsTo);
//    }
//    
//    @Override
//    @Deprecated
//    public String getContentsOf(String url) throws HttpException {
//        throw new UnsupportedOperationException();
//    }
//    
//    @Override
//    public <T> T get(SimpleHttpRequest<T> request) throws HttpException, Exception {
//        URL url = respondsTo.get(request.getUrl());
//        InputStream body = Resources.newInputStreamSupplier(url).getInput();
//        return request.getTransformer().transform(HttpResponsePrologue.sucessfulResponse(), body);
//    }
//    
//    @Override
//    @Deprecated
//    public HttpResponse get(String url) throws HttpException {
//        throw new UnsupportedOperationException();
//    }
//    
//    @Override
//    public HttpResponse post(String url, Payload data) throws HttpException {
//        throw new UnsupportedOperationException();
//    }
//    
//    @Override
//    public HttpResponse head(String string) throws HttpException {
//        throw new UnsupportedOperationException();
//    }
//    
//    @Override
//    public HttpResponse delete(String string) throws HttpException {
//        throw new UnsupportedOperationException();
//    }
//    
//    @Override
//    public HttpResponse put(String url, Payload data) throws HttpException {
//        throw new UnsupportedOperationException();
//    }
    
}
