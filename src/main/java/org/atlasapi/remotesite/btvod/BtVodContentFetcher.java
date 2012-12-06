package org.atlasapi.remotesite.btvod;

import static org.atlasapi.remotesite.btvod.BtVodItemDataExtractor.HREF_KEY;
import static org.atlasapi.remotesite.btvod.BtVodItemDataExtractor.SELF_KEY;
import static org.atlasapi.remotesite.btvod.BtVodItemDataExtractor.getLinkElement;

import java.io.InputStream;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

import com.google.common.collect.Queues;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpClientBuilder;
import com.metabroadcast.common.http.SimpleHttpRequest;
import com.metabroadcast.common.media.MimeType;
import com.metabroadcast.common.security.UsernameAndPassword;

public class BtVodContentFetcher {
    private static final String CONTENT_ANNOTATIONS = "?expand=availability_windows,platforms,series,assets,metadata";
    
    private final String url;
    private final SimpleHttpClient client;
    private final Queue<String> contentUrls;
    private final BtVodHttpResponseTransformer contentResponseTransformer = new BtVodHttpResponseTransformer();

    public BtVodContentFetcher(String url, String username, String password, int timeout) {
        this.url = url;
        UsernameAndPassword creds = new UsernameAndPassword(username, password);
        SimpleHttpClientBuilder httpClientBuilder = new SimpleHttpClientBuilder();
        this.client = httpClientBuilder
                .withSocketTimeout(timeout, TimeUnit.SECONDS)
                .withAcceptHeader(MimeType.APPLICATION_XML)
                .withDigestAuth(creds)
                .build();
        contentUrls = Queues.newConcurrentLinkedQueue();
    }
    
    public Document getContent() throws HttpException, Exception {
        if (!contentUrls.isEmpty()) {
            client.get(new SimpleHttpRequest<Void>(contentUrls.poll() + CONTENT_ANNOTATIONS, contentResponseTransformer));
            return contentResponseTransformer.getXml();
        }
        return null;
    }
    
    public void getLatestContentList() throws HttpException, Exception {
        contentUrls.clear();
        client.get(new SimpleHttpRequest<Void>(url, contentResponseTransformer));
        Element root = contentResponseTransformer.getXml().getRootElement();
        for (int i = 0; i < root.getChildElements().size(); i++) {
            Element child = root.getChildElements().get(i);
            Element selfLink = getLinkElement(child, SELF_KEY);
            contentUrls.add(selfLink.getAttributeValue(HREF_KEY));
        }
    }
    
    private class BtVodHttpResponseTransformer implements HttpResponseTransformer<Void> {
        private Document xml;
        
        @Override
        public Void transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, Exception {
            xml = new Builder().build(body);
            return null;
        }
        
        public Document getXml() {
            return xml;
        }
    }
}
