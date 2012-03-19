package org.atlasapi.remotesite.space;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import java.io.InputStream;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 */
public class JSonNodeHttpResponseTransformer implements HttpResponseTransformer<JsonNode> {
    
    private final ObjectMapper mapper;
    
    public JSonNodeHttpResponseTransformer(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public JsonNode transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, Exception {
        return mapper.readTree(body);
    }
}
