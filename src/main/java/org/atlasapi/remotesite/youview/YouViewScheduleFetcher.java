package org.atlasapi.remotesite.youview;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpClientBuilder;
import com.metabroadcast.common.http.SimpleHttpRequest;
import com.metabroadcast.common.url.QueryStringParameters;

import nu.xom.Builder;
import nu.xom.Document;

public class YouViewScheduleFetcher {
    
    private final String youviewUrl;
    private final SimpleHttpClient client;
    private final XmlHttpResponseTransformer xmlTransformer = new XmlHttpResponseTransformer();
    
    public YouViewScheduleFetcher(String youviewUrl, int timeout) {
        this.youviewUrl = youviewUrl;
        client = new SimpleHttpClientBuilder().withSocketTimeout(timeout, TimeUnit.SECONDS).build();
    }

    // fetch schedule from start to finish
    public Document getSchedule(DateTime start, DateTime finish) throws HttpException, Exception {
        QueryStringParameters qsp = new QueryStringParameters();
        qsp.add("service", "" + 1044);
        qsp.add("starttime", start.toString());
        qsp.add("endtime", finish.toString());
        client.get(new SimpleHttpRequest<Void>(youviewUrl + "?" + qsp.toQueryString(), xmlTransformer));
        return xmlTransformer.getXml();
    }
    
    private class XmlHttpResponseTransformer implements HttpResponseTransformer<Void> {
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
