package org.uriplay.remotesite.http;

import java.io.Reader;

import org.uriplay.persistence.system.RemoteSiteClient;

public interface BasicHttpClient extends RemoteSiteClient<Reader> {

	Reader post(String uri, String content) throws Exception;
}
