package org.atlasapi.remotesite.talktalk;

import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.atlasapi.remotesite.talktalk.vod.bindings.TVDataInterfaceResponse;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Resources;

public class JaxbTalkTalkTvDataInterfaceResponseParserTest {
    
    private JaxbTalkTalkTvDataInterfaceResponseParser parser;
    
    @Before
    public void setUp() throws Exception {
        SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        Optional<Schema> schema = Optional.of(schemaFactory.newSchema(Resources.getResource("TVDataInterfaceModel.xsd")));
        JAXBContext context = JAXBContext.newInstance("org.atlasapi.remotesite.talktalk.vod.bindings");
        parser = new JaxbTalkTalkTvDataInterfaceResponseParser(context, schema);
    }
    
    @Test
    public void testParsingTvStructure() throws Exception {
        URL structure = Resources.getResource(getClass(),"structure.xml");
        Unmarshaller.Listener listener = mock(Unmarshaller.Listener.class);
        parser.parse(Resources.newReaderSupplier(structure, Charsets.UTF_8).getInput(), listener);
        
        verify(listener).afterUnmarshal(argThat(is(TVDataInterfaceResponse.class)), any());
    }
    
    @Test
    public void testParsingVodList() throws Exception {
        URL structure = Resources.getResource(getClass(),"vod-list.xml");
        Unmarshaller.Listener listener = mock(Unmarshaller.Listener.class);
        parser.parse(Resources.newReaderSupplier(structure, Charsets.UTF_8).getInput(), listener);
        
        verify(listener).afterUnmarshal(argThat(is(TVDataInterfaceResponse.class)), any());
    }
    
    @Test
    public void testParsingDetail() throws Exception {
        URL structure = Resources.getResource(getClass(),"detail.xml");
        Unmarshaller.Listener listener = mock(Unmarshaller.Listener.class);
        parser.parse(Resources.newReaderSupplier(structure, Charsets.UTF_8).getInput(), listener);
        
        verify(listener).afterUnmarshal(argThat(is(TVDataInterfaceResponse.class)), any());
    }
    
}
