package org.atlasapi.codec;

import java.math.BigInteger;

public class IdCodec {

    private final String id;
    
    public IdCodec(String id) {
        this.id = id;
    }

    public IdCodec(BigInteger id) {
        this.id = id.toString();
    }
    
    public String getId() {
        return id;
    }
    
}
