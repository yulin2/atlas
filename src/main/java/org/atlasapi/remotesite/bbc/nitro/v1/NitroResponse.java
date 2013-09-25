package org.atlasapi.remotesite.bbc.nitro.v1;

import com.google.api.client.util.Key;

public class NitroResponse<T> {

    @Key private NitroResultHolder<T> nitro;

    public NitroResultHolder<T> getNitro() {
        return nitro;
    }

    public void setNitro(NitroResultHolder<T> nitro) {
        this.nitro = nitro;
    }

    @Override
    public String toString() {
        return nitro.toString();
    }
    
}
