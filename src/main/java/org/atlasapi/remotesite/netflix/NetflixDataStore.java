package org.atlasapi.remotesite.netflix;

import java.io.InputStream;

import nu.xom.Document;

public interface NetflixDataStore {
    
    void save(InputStream dataStream) throws Exception;
    
    Document getData();
}
