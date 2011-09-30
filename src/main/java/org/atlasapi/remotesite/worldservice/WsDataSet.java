package org.atlasapi.remotesite.worldservice;


public interface WsDataSet {
    
    String getName();
    
    WsDataSource getAudioItem();
    
    WsDataSource getAudioItemProgLink();
    
    WsDataSource getGenre();
    
    WsDataSource getProgramme();
    
    WsDataSource getSeries();

}
