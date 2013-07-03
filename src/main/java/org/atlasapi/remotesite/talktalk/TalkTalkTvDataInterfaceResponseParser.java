package org.atlasapi.remotesite.talktalk;

import java.io.Reader;

import javax.xml.bind.Unmarshaller.Listener;

import org.atlasapi.remotesite.talktalk.vod.bindings.TVDataInterfaceResponse;

/**
 * Can parse XML data read from TalkTalk into a {@link TvDataInterfaceResponse}. 
 */
public interface TalkTalkTvDataInterfaceResponseParser {

    TVDataInterfaceResponse parse(Reader reader, Listener listener) throws Exception;
    
}