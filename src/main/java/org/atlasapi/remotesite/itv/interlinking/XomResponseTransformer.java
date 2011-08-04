package org.atlasapi.remotesite.itv.interlinking;

import java.io.InputStream;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.NodeFactory;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;

public class XomResponseTransformer implements HttpResponseTransformer<Document> {
    
    private final Builder builder;
    
    public static XomResponseTransformer standard() {
        return new XomResponseTransformer(new NodeFactory());
    }
    
    public static XomResponseTransformer withNodeFactory(NodeFactory nodeFactory) {
        return new XomResponseTransformer(nodeFactory);
    }
    
    private XomResponseTransformer(NodeFactory nodeFactory) {
        this.builder = new Builder(nodeFactory);
    }
    
    @Override
    public Document transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, Exception {
        return builder.build(body);
    }
}
