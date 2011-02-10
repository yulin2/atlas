package org.atlasapi.remotesite.channel4;

import java.io.StringReader;

import nu.xom.Builder;
import nu.xom.Document;

import org.atlasapi.persistence.system.RemoteSiteClient;

import com.metabroadcast.common.http.SimpleHttpClient;

public class XmlClient implements RemoteSiteClient<Document> {
    
    private final SimpleHttpClient client;
    private final Builder builder;
    
    public XmlClient(SimpleHttpClient client) {
        this(client, new Builder());
    }

    public XmlClient(SimpleHttpClient client, Builder builder) {
        this.client = client;
        this.builder = builder;
    }

    @Override
    public Document get(String uri) throws Exception {
        return builder.build(new StringReader(client.getContentsOf(uri)));
    }

}
