package org.atlasapi.remotesite.talktalk;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.InputStreamReader;

import javax.annotation.Nullable;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Unmarshaller.Listener;

import org.atlasapi.http.AbstractHttpResponseTransformer;
import org.atlasapi.remotesite.talktalk.vod.bindings.ChannelType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemTypeType;
import org.atlasapi.remotesite.talktalk.vod.bindings.TVDataInterfaceResponse;
import org.atlasapi.remotesite.talktalk.vod.bindings.VODEntityType;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.net.HostSpecifier;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;
import com.metabroadcast.common.url.QueryStringParameters;
import com.metabroadcast.common.url.Urls;

/**
 * XML-over-HTTP-based {@link TalkTalkClient} 
 */
public class XmlTalkTalkClient implements TalkTalkClient {

    private final <T> AbstractHttpResponseTransformer<T> transformer(Unmarshaller.Listener listener, Function<? super TVDataInterfaceResponse, ? extends T> continuation) {
        return new JaxbListeningTaklTalkResponseTransformer<T>(listener, continuation);
    }
    
    private final class JaxbListeningTaklTalkResponseTransformer<T> extends
            AbstractHttpResponseTransformer<T> {
        
        private final Listener listener;
        private final Function<? super TVDataInterfaceResponse, ? extends T> continuation;
        
        private JaxbListeningTaklTalkResponseTransformer(Unmarshaller.Listener listener, Function<? super TVDataInterfaceResponse, ? extends T> continuation) {
            this.listener = checkNotNull(listener);
            this.continuation = checkNotNull(continuation);
        }
        
        @Override
        protected T transform(InputStreamReader bodyReader) throws Exception {
            return continuation.apply(parser.parse(bodyReader, listener));
        }
        
    }

    private static final Integer UNKNOWN = null;

    private final SimpleHttpClient client;
    private final HostSpecifier host;
    private final TalkTalkTvDataInterfaceResponseParser parser;

    public XmlTalkTalkClient(SimpleHttpClient client, HostSpecifier host, TalkTalkTvDataInterfaceResponseParser parser) {
        this.client = checkNotNull(client);
        this.host = checkNotNull(host);
        this.parser = checkNotNull(parser);
    }

    @Override
    public <R> R processTvStructure(final TalkTalkTvStructureProcessor<R> processor) throws TalkTalkException {
        String url = String.format("http://%s/TVDataInterface/TVStructure/Structure/1", host.toString());
        try {
            client.get(SimpleHttpRequest.httpRequestFrom(
                url,
                transformer(adapt(processor), Functions.<Void>constant(null))
            ));
            return processor.getResult();
        } catch (Exception e) {
            throw new TalkTalkException(url, e);
        }
    }
    
    private Unmarshaller.Listener adapt(final TalkTalkTvStructureProcessor<?> processor) {
        return new Unmarshaller.Listener() {
            @Override
            public void afterUnmarshal(Object target, Object parent) {
                if (target instanceof ChannelType) {
                    processor.process((ChannelType)target);
                }
            }
        };
    }

    @Override
    public <R> R processVodList(ItemTypeType type, String identifier,
            TalkTalkVodEntityProcessor<R> processor, int itemsPerPage) throws TalkTalkException {
        String url = Urls.appendParameters(String.format("http://%s/TVDataInterface/VOD/List/2?", host.toString()), parameters(type, identifier));
        int page = 0;
        Integer expected = UNKNOWN;
        do {
            expected = getVodPage(url, processor, itemsPerPage, page);
            page++;
        } while (expected != UNKNOWN && (page * itemsPerPage) < expected);
        return processor.getResult();
    }

    private static final Function<TVDataInterfaceResponse, Integer> VOD_LIST_ENTITY_COUNT
        = new Function<TVDataInterfaceResponse, Integer>() {
            @Override
            public Integer apply(@Nullable TVDataInterfaceResponse input) {
                return input.getVodList().getTotalEntityCount();
            }
        };

    private Integer getVodPage(String url,
            TalkTalkVodEntityProcessor<?> processor, int itemsPerPage, int page)
            throws TalkTalkException {
        String paginatedUrl = Urls.appendParameters(url, selection(page, itemsPerPage));
        try {
            return client.get(new SimpleHttpRequest<Integer>(
                paginatedUrl, transformer(adapt(processor), VOD_LIST_ENTITY_COUNT)
            ));
        } catch (Exception e) {
            throw new TalkTalkException(paginatedUrl, e);
        }
    }
    
    private QueryStringParameters parameters(ItemTypeType type, String identifier) {
        return QueryStringParameters
            .parameters("groupType", type.toString())
            .add("groupIdentifier", identifier);
    }
    
    private QueryStringParameters selection(int page, int itemsPerPage) {
        return QueryStringParameters
                .parameters("page", String.valueOf(page))
                .add("itemsPerPage", String.valueOf(itemsPerPage));
    }

    private Unmarshaller.Listener adapt(final TalkTalkVodEntityProcessor<?> processor) {
        return new Unmarshaller.Listener() {
            @Override
            public void afterUnmarshal(Object target, Object parent) {
                if (target instanceof VODEntityType) {
                    processor.process((VODEntityType)target);
                }
            }
        };
    }
    
    @Override
    public ItemDetailType getItemDetail(ItemTypeType type, String identifier)
            throws TalkTalkException {
        String url = Urls.appendParameters(String.format("http://%s/TVDataInterface/Detail/Item/2?", host.toString()), parameters(type, identifier));
        try {
            return client.get(SimpleHttpRequest.httpRequestFrom(
                url, transformer(NOOP_LISTENER, RESPONSE_ITEM_DETAIL)
            ));
        } catch (Exception e) {
            throw new TalkTalkException(url, e);
        }
    }
    
    private final static Unmarshaller.Listener NOOP_LISTENER = new Unmarshaller.Listener() {};
    
    private final static Function<TVDataInterfaceResponse, ItemDetailType> RESPONSE_ITEM_DETAIL
        = new Function<TVDataInterfaceResponse, ItemDetailType>() {
            @Override
            public ItemDetailType apply(@Nullable TVDataInterfaceResponse input) {
                return input.getItemDetail();
            }
        };
}
