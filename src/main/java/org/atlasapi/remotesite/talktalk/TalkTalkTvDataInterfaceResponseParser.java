package org.atlasapi.remotesite.talktalk;

import java.io.Reader;

import javax.xml.bind.Unmarshaller.Listener;

import org.atlasapi.remotesite.talktalk.vod.bindings.TVDataInterfaceResponse;

/**
 * Parses {@link TVDataInterfaceResponse}s. 
 */
public interface TalkTalkTvDataInterfaceResponseParser {

    void parse(Reader reader, Listener listener) throws Exception;
    
}