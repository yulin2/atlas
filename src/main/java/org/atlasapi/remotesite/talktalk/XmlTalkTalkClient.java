package org.atlasapi.remotesite.talktalk;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.InputStreamReader;

import javax.xml.bind.Unmarshaller;

import org.atlasapi.http.AbstractHttpResponseTransformer;
import org.atlasapi.remotesite.talktalk.vod.bindings.ChannelType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
import org.atlasapi.remotesite.talktalk.vod.bindings.TVDataInterfaceResponse;
import org.atlasapi.remotesite.talktalk.vod.bindings.VODEntityType;

import com.google.common.net.HostSpecifier;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;
import com.metabroadcast.common.url.QueryStringParameters;
import com.metabroadcast.common.url.Urls;

/**
 * XML-over-HTTP-based {@link TalkTalkClient} 
 */
public class XmlTalkTalkClient implements TalkTalkClient {

    private static final int DEFAULT_ITEMS_PER_PAGE = 500;

    private final <T> AbstractHttpResponseTransformer<T> transformer(TvDataInterfaceResponseListener<T> listener) {
        return new JaxbListeningTalkTalkResponseTransformer<T>(listener);
    }
    
    /*
     * Parses the HTTP response with the parser. The parsing is listened to with
     * the provided listener. A final continuation is applied to the final
     * TVDataInterfaceResponse to project a value from it. 
     */
    private final class JaxbListeningTalkTalkResponseTransformer<T> extends
            AbstractHttpResponseTransformer<T> {
        
        private final TvDataInterfaceResponseListener<T> listener;
        
        private JaxbListeningTalkTalkResponseTransformer(TvDataInterfaceResponseListener<T> listener) {
            this.listener = checkNotNull(listener);
        }
        
        @Override
        protected T transform(InputStreamReader bodyReader) throws Exception {
            parser.parse(bodyReader, listener);
            return listener.getResult();
        }
        
    }
    
    private static abstract class TvDataInterfaceResponseListener<T> extends Unmarshaller.Listener {
        
        abstract T getResult(); 
        
    }

    private static final Integer UNKNOWN = null;

    private final SimpleHttpClient client;
    private final HostSpecifier host;
    private final TalkTalkTvDataInterfaceResponseParser parser;
    private final int itemsPerPage;


    public XmlTalkTalkClient(SimpleHttpClient client, HostSpecifier host, TalkTalkTvDataInterfaceResponseParser parser, int itemsPerPage) {
        this.client = checkNotNull(client);
        this.host = checkNotNull(host);
        this.parser = checkNotNull(parser);
        this.itemsPerPage = itemsPerPage;
    }
    
    public XmlTalkTalkClient(SimpleHttpClient client, HostSpecifier host, TalkTalkTvDataInterfaceResponseParser parser) {
        this(client, host, parser, DEFAULT_ITEMS_PER_PAGE);
    }

    @Override
    public <R> R processTvStructure(final TalkTalkTvStructureCallback<R> callback) throws TalkTalkException {
        String url = String.format("http://%s/TVDataInterface/TVStructure/Structure/4", host.toString());
        try {
            client.get(SimpleHttpRequest.httpRequestFrom(
                url,
                transformer(toStructureUnmarshallListener(callback))
            ));
            return callback.getResult();
        } catch (Exception e) {
            throw new TalkTalkException(url, e);
        }
    }
    
    private TvDataInterfaceResponseListener<Void> toStructureUnmarshallListener(
            final TalkTalkTvStructureCallback<?> callback) {
        return new TvDataInterfaceResponseListener<Void>() {

            @Override
            public void afterUnmarshal(Object target, Object parent) {
                if (target instanceof ChannelType) {
                    callback.process((ChannelType) target);
                }
            }

            @Override
            Void getResult() {
                return null;
            }
        };
    }

    @Override
    public <R> R processVodList(GroupType type, String identifier,
            TalkTalkVodListCallback<R> callback) throws TalkTalkException {
        String url = Urls.appendParameters(String.format("http://%s/TVDataInterface/VOD/List/2?", host.toString()), parameters(type, identifier));
        int page = 0;
        Integer expected = UNKNOWN;
        do {
            expected = getVodPage(url, callback, itemsPerPage, page);
            page++;
        } while (expected != UNKNOWN && (page * itemsPerPage) < expected);
        return callback.getResult();
    }

    private Integer getVodPage(String url,
            TalkTalkVodListCallback<?> callback, int itemsPerPage, int page)
            throws TalkTalkException {
        String paginatedUrl = Urls.appendParameters(url, selection(page, itemsPerPage));
        try {
            return client.get(new SimpleHttpRequest<Integer>(
                paginatedUrl, transformer(toListUnmarshallListener(callback))
            ));
        } catch (Exception e) {
            throw new TalkTalkException(paginatedUrl, e);
        }
    }
    
    private QueryStringParameters parameters(GroupType type, String identifier) {
        return QueryStringParameters
            .parameters("groupType", type.toString())
            .add("groupIdentifier", identifier);
    }
    
    private QueryStringParameters selection(int page, int itemsPerPage) {
        return QueryStringParameters
                .parameters("page", String.valueOf(page))
                .add("itemsPerPage", String.valueOf(itemsPerPage));
    }

    private TvDataInterfaceResponseListener<Integer> toListUnmarshallListener(final TalkTalkVodListCallback<?> callback) {
        return new TvDataInterfaceResponseListener<Integer>() {
            
            Integer result = null;
            
            @Override
            public void afterUnmarshal(Object target, Object parent) {
                if (target instanceof VODEntityType) {
                    callback.process((VODEntityType)target);
                }
                if (target instanceof TVDataInterfaceResponse) {
                    TVDataInterfaceResponse resp = (TVDataInterfaceResponse) target;
                    result = resp.getVodList().getTotalEntityCount();
                }
            }

            @Override
            Integer getResult() {
                return result;
            }
        };
    }
    
    @Override
    public ItemDetailType getItemDetail(GroupType type, String identifier)
            throws TalkTalkException {
        // hdEnabled parameter is a temporary workaround for a bug in the TalkTalk API
        String url = Urls.appendParameters(String.format("http://%s/TVDataInterface/Detail/Item/2?", host.toString()), 
                parameters(type, identifier).add("hdEnabled", "false"));
        try {
            return client.get(SimpleHttpRequest.httpRequestFrom(
                url, transformer(ITEM_DETAIL_LISTENER)
            ));
        } catch (Exception e) {
            throw new TalkTalkException(url, e);
        }
    }

    private final static TvDataInterfaceResponseListener<ItemDetailType> ITEM_DETAIL_LISTENER
        = new TvDataInterfaceResponseListener<ItemDetailType>() {

        private ItemDetailType result;

        @Override
            public void afterUnmarshal(Object target, Object parent) {
                if (target instanceof TVDataInterfaceResponse) {
                    TVDataInterfaceResponse resp = (TVDataInterfaceResponse) target;
                    result = resp.getItemDetail();
                }
            }
        
        @Override
        ItemDetailType getResult() {
            return result;
        }
    };

}
