package org.atlasapi.remotesite.btfeatured;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStreamReader;

import nu.xom.Builder;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;

@RunWith(MockitoJUnitRunner.class)
public class TestBTContentUpdater {

    private static final String PRODUCT_BASE_URL = "http://test.bt.featured/product/";

    private static String ROOT_DOCUMENT_URL = "http://test.bt.featured";
    
    @Mock
    BTFeaturedClient client;
    
    BTFeaturedElementHandler handler;
    
    @Mock
    ContentResolver contentResolver;
    
    @Mock
    ContentWriter contentWriter;
    
    @Mock
    ContentGroupResolver contentGroupResolver;
    
    @Mock
    ContentGroupWriter contentGroupWriter;
    
    @Mock
    ResolvedContent resolvedContent;

    @Mock 
    Maybe<Identified> identified;
    
    @Before
    public void setup() throws ValidityException, IOException, ParsingException, Exception {
        BTFeaturedNodeFactory factory = new BTFeaturedNodeFactory();     

        InputStreamReader bodyReader = new InputStreamReader(getClass().getResourceAsStream("topLevelProducts.xml"));
        
        when(client.get(ROOT_DOCUMENT_URL)).thenReturn(BTFeaturedClient.readDocument(ROOT_DOCUMENT_URL, bodyReader, new Builder(factory)));
        
        when(contentGroupResolver.findByCanonicalUris(ImmutableList.of(BTFeaturedContentUpdater.CONTENT_GROUP_URI))).thenReturn(resolvedContent);
        when(resolvedContent.get(any(String.class))).thenReturn(identified);
        when(identified.hasValue()).thenReturn(false);
        
        bodyReader = new InputStreamReader(getClass().getResourceAsStream("singleProduct_954440.xml"));
        when(client.get(PRODUCT_BASE_URL+"954440.xml")).thenReturn(BTFeaturedClient.readDocument(PRODUCT_BASE_URL+"954440.xml", bodyReader, new Builder(factory)));

        bodyReader = new InputStreamReader(getClass().getResourceAsStream("collection_925860.xml"));
        when(client.get(PRODUCT_BASE_URL+"925860.xml")).thenReturn(BTFeaturedClient.readDocument(PRODUCT_BASE_URL+"925860.xml", bodyReader, new Builder(factory)));
 
        bodyReader = new InputStreamReader(getClass().getResourceAsStream("childProduct_925858.xml"));
        when(client.get(PRODUCT_BASE_URL+"925858.xml")).thenReturn(BTFeaturedClient.readDocument(PRODUCT_BASE_URL+"925858.xml", bodyReader, new Builder(factory)));

        bodyReader = new InputStreamReader(getClass().getResourceAsStream("childProduct_936814.xml"));
        when(client.get(PRODUCT_BASE_URL+"936814.xml")).thenReturn(BTFeaturedClient.readDocument(PRODUCT_BASE_URL+"936814.xml", bodyReader, new Builder(factory)));

        handler = new BTFeaturedElementHandler();

        when(contentResolver.findByCanonicalUris(any(Iterable.class))).thenReturn(resolvedContent);
        when(resolvedContent.resolved(any(String.class))).thenReturn(false);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testParsesTopLevelProducts() {
        assertNotNull(handler);
        BTFeaturedContentUpdater updater = new BTFeaturedContentUpdater(client, handler, contentGroupResolver, contentGroupWriter, contentResolver, contentWriter, PRODUCT_BASE_URL, ROOT_DOCUMENT_URL);
        
        updater.run();
        
    }

}
