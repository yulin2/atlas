package org.atlasapi.remotesite.itv.whatson;

import java.util.Collection;

import org.atlasapi.persistence.system.RemoteSiteClient;

import com.metabroadcast.common.http.SimpleHttpClient;

public class ItvWhatsOnClient implements RemoteSiteClient<Collection<ItvWhatsOnEntryDuration>> {
    private final SimpleHttpClient httpClient;
    
    public ItvWhatsOnClient(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Collection<ItvWhatsOnEntryDuration> get(String uri) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
