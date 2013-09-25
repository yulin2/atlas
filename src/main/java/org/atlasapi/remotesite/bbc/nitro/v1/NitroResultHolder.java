package org.atlasapi.remotesite.bbc.nitro.v1;

import com.google.api.client.util.Key;

public class NitroResultHolder<T> {

    @Key private NitroResults<T> results;

    public NitroResults<T> getResults() {
        return results;
    }

    public void setResults(NitroResults<T> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return results.toString();
    }
    
}
