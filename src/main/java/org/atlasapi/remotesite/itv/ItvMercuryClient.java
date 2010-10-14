package org.atlasapi.remotesite.itv;

import java.util.Map;

import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.HttpClients;
import org.codehaus.jackson.map.ObjectMapper;

import com.metabroadcast.common.http.SimpleHttpClient;

public class ItvMercuryClient implements RemoteSiteClient<Map<String, Object>> {
    
    private final SimpleHttpClient httpClient;
    private final ObjectMapper mapper;
    
    public ItvMercuryClient() {
        this(HttpClients.webserviceClient(), new ObjectMapper());
    }

    public ItvMercuryClient(SimpleHttpClient httpClient, ObjectMapper mapper) {
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> get(String url) throws Exception {
        String contents = httpClient.getContentsOf(url);
        return mapper.readValue(contents, Map.class);
    }
}
