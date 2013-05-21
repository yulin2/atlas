package org.atlasapi.remotesite.lovefilm;

import java.io.IOException;
import java.io.InputStream;


public interface LoveFilmFileStore {

    void save(String fileName, InputStream dataStream) throws IOException;
    
    LoveFilmData fetchLatestData();
}
