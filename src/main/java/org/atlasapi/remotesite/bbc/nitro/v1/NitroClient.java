package org.atlasapi.remotesite.bbc.nitro.v1;

import java.util.List;

import org.atlasapi.remotesite.bbc.nitro.NitroException;


public interface NitroClient {

    List<NitroGenreGroup> genres(String pid) throws NitroException;
    
    List<NitroFormat> formats(String pid) throws NitroException;
    
}
