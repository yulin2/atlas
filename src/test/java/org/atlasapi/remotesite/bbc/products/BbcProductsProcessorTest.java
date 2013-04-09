package org.atlasapi.remotesite.bbc.products;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.product.Product;
import org.atlasapi.media.product.ProductStore;
import org.atlasapi.s3.S3Client;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Optional;
import com.google.common.io.Files;
import com.metabroadcast.common.base.Maybe;

/**
 */
public class BbcProductsProcessorTest {

    @Test
    public void testProcess() throws Exception {
        S3Client client = mock(S3Client.class);
        when(client.getAndSaveIfUpdated(eq(BbcProductsProcessor.PRODUCTS), any(File.class), any(Maybe.class))).thenAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                File f = (File) invocation.getArguments()[1];
                Files.copy(new File(BbcProductsProcessorTest.class.getClassLoader().getResource("bbc-products.txt").getFile()), f);
                return Boolean.TRUE;
            }
        });
        when(client.getAndSaveIfUpdated(eq(BbcProductsProcessor.LOCATIONS), any(File.class), any(Maybe.class))).thenAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                File f = (File) invocation.getArguments()[1];
                Files.copy(new File(BbcProductsProcessorTest.class.getClassLoader().getResource("bbc-locations.txt").getFile()), f);
                return Boolean.TRUE;
            }
        });;
        when(client.getAndSaveIfUpdated(eq(BbcProductsProcessor.BRANDS), any(File.class), any(Maybe.class))).thenAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                File f = (File) invocation.getArguments()[1];
                Files.copy(new File(BbcProductsProcessorTest.class.getClassLoader().getResource("bbc-brands.txt").getFile()), f);
                return Boolean.TRUE;
            }
        });;
        when(client.getAndSaveIfUpdated(eq(BbcProductsProcessor.SERIES), any(File.class), any(Maybe.class))).thenAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                File f = (File) invocation.getArguments()[1];
                Files.copy(new File(BbcProductsProcessorTest.class.getClassLoader().getResource("bbc-series.txt").getFile()), f);
                return Boolean.TRUE;
            }
        });;
        when(client.getAndSaveIfUpdated(eq(BbcProductsProcessor.EPISODES), any(File.class), any(Maybe.class))).thenAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                File f = (File) invocation.getArguments()[1];
                Files.copy(new File(BbcProductsProcessorTest.class.getClassLoader().getResource("bbc-episodes.txt").getFile()), f);
                return Boolean.TRUE;
            }
        });

        ProductStore store = mock(ProductStore.class);
        when(store.productForSourceIdentified(eq(Publisher.BBC_PRODUCTS), anyString())).thenReturn(Optional.<Product>absent());

        ArgumentCaptor<Product> product = ArgumentCaptor.forClass(Product.class);

        BbcProductsProcessor processor = new BbcProductsProcessor();

        processor.process(client, store);

        verify(store, times(3)).store(product.capture());
    }
}
