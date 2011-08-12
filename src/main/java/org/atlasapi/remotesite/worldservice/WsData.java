package org.atlasapi.remotesite.worldservice;

import java.io.InputStream;

public interface WsData {
    
    InputStream getAudioItem();
    
    InputStream getAudioItemProgLink();
    
    InputStream getGenre();
    
    InputStream getProgramme();
    
    InputStream getSeries();

}
