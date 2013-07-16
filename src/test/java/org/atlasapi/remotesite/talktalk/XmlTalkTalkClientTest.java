package org.atlasapi.remotesite.talktalk;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.bind.Unmarshaller;

import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemTypeType;
import org.atlasapi.remotesite.talktalk.vod.bindings.TVDataInterfaceResponse;
import org.atlasapi.remotesite.talktalk.vod.bindings.VodListType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.common.net.HostSpecifier;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;

@RunWith(MockitoJUnitRunner.class)
public class XmlTalkTalkClientTest {
    
    private HostSpecifier host;
    private SimpleHttpClient client;
    private TalkTalkTvDataInterfaceResponseParser parser;
    
    private TalkTalkClient ttClient;
    
    @Mock private TalkTalkTvStructureProcessor<Integer> structureProcessor;
    @Mock private TalkTalkVodEntityProcessor<Integer> vodEntityProcessor;
    
    @Before
    public void setUp() {
        host = HostSpecifier.fromValid("localhost.com");
        parser = mock(TalkTalkTvDataInterfaceResponseParser.class);
        
        client = new StubSimpleHttpClient(ImmutableMap.of(
            "http://localhost.com/TVDataInterface/TVStructure/Structure/1",
            Resources.getResource(getClass(), "structure.xml"),
            "http://localhost.com/TVDataInterface/VOD/List/2?groupType=BRAND&groupIdentifier=ABCCASTL&page=0&itemsPerPage=5",
            Resources.getResource(getClass(), "vod-list.xml"),
            "http://localhost.com/TVDataInterface/VOD/List/2?groupType=BRAND&groupIdentifier=ABCCASTL&page=0&itemsPerPage=3",
            Resources.getResource(getClass(), "vod-list.xml"),
            "http://localhost.com/TVDataInterface/VOD/List/2?groupType=BRAND&groupIdentifier=ABCCASTL&page=1&itemsPerPage=3",
            Resources.getResource(getClass(), "vod-list.xml"),
            "http://localhost.com/TVDataInterface/Detail/Item/2?groupType=EPISODE&groupIdentifier=397212",
            Resources.getResource(getClass(), "detail.xml")
        ));
        
        ttClient = new XmlTalkTalkClient(client, host, parser);
    }
    
    @Test
    public void testProcessTvStructure() throws HttpException, Exception {

        final TVDataInterfaceResponse response = new TVDataInterfaceResponse();
        
        when(parser.parse(any(Reader.class), any(Unmarshaller.Listener.class)))
            .thenReturn(response);
        when(structureProcessor.getResult())
            .thenReturn(42);
        
        Integer processedResult = ttClient.processTvStructure(structureProcessor);

        verify(structureProcessor).getResult();
        verify(parser).parse(any(Reader.class), any(Unmarshaller.Listener.class));
        
        assertThat(processedResult, is(42));
    }

    @Test
    public void testProcessVodList() throws HttpException, Exception {
        
        TVDataInterfaceResponse response = new TVDataInterfaceResponse();
        VodListType vodList = new VodListType();
        vodList.setTotalEntityCount(4);
        response.setVodList(vodList);
        
        when(parser.parse(any(Reader.class), any(Unmarshaller.Listener.class)))
            .thenReturn(response);
        when(vodEntityProcessor.getResult())
            .thenReturn(42);
        
        Integer processedResult = ttClient.processVodList(ItemTypeType.BRAND, "ABCCASTL", vodEntityProcessor, 5);
        
        verify(vodEntityProcessor).getResult();
        verify(parser).parse(any(Reader.class), any(Unmarshaller.Listener.class));
        
        assertThat(processedResult, is(42));
    }

    @Test
    public void testProcessVodListWithMultipleRequests() throws HttpException, Exception {
        
        TVDataInterfaceResponse response = new TVDataInterfaceResponse();
        VodListType vodList = new VodListType();
        vodList.setTotalEntityCount(4);
        response.setVodList(vodList);
        
        when(parser.parse(any(Reader.class), any(Unmarshaller.Listener.class)))
            .thenReturn(response);
        when(parser.parse(any(Reader.class), any(Unmarshaller.Listener.class)))
            .thenReturn(response);
        when(vodEntityProcessor.getResult()).thenReturn(42);
        
        Integer processedResult = ttClient.processVodList(ItemTypeType.BRAND, "ABCCASTL", vodEntityProcessor, 3);
        
        verify(vodEntityProcessor).getResult();
        verify(parser, times(2)).parse(any(Reader.class), any(Unmarshaller.Listener.class));
        
        assertThat(processedResult, is(42));
    }

    @Test
    public void testGetItemDetail() throws HttpException, Exception {
        
        TVDataInterfaceResponse response = new TVDataInterfaceResponse();
        ItemDetailType detail = new ItemDetailType();
        detail.setTitle("title");
        response.setItemDetail(detail);
        
        when(parser.parse(any(Reader.class), any(Unmarshaller.Listener.class)))
            .thenReturn(response);
        
        ItemDetailType fetchedDetail = ttClient.getItemDetail(ItemTypeType.EPISODE, "397212");
        
        assertThat(fetchedDetail.getTitle(), is("title"));
    }

}
