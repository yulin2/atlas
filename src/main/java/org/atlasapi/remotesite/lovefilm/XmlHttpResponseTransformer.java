package org.atlasapi.remotesite.lovefilm;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import java.io.InputStream;
import nu.xom.Builder;
import nu.xom.Document;

/**
 */
public class XmlHttpResponseTransformer implements HttpResponseTransformer<Document> {

    @Override
    public Document transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, Exception {
        return new Builder().build(body);
    }
}
