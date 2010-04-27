/* Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */


package org.uriplay.remotesite.bliptv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.http.CommonsHttpClient;
import org.jherd.remotesite.http.RemoteSiteClient;

/**
 * Client to retrieve JSON fragment containing the embed code for a clip from blip.tv
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BlipTvEmbedCodeClient implements RemoteSiteClient<String> {

	private static final String EMBED_CODE_SOURCE = "http://blip.tv/players/embed/?posts_id=%s&players_id=-1&skin=json&callback=DoSomethingActions.playerSelector.gotEmbedCode";
	private final RemoteSiteClient<Reader> httpClient;

	public BlipTvEmbedCodeClient(RemoteSiteClient<Reader> httpClient) {
		this.httpClient = httpClient;
	}
	
	public BlipTvEmbedCodeClient() {
		this(new CommonsHttpClient());
	}

	public String get(String videoSourceUri) throws Exception {
		Reader embedJson = httpClient.get(String.format(EMBED_CODE_SOURCE, postsIdFrom(videoSourceUri)));
		return htmlEmbedCodeFrom(embedJson);
	}

	/**
	 * from a call to blip at the above url we can get a json snippet from which we can extract a suitable embed code.
	 */
	private String htmlEmbedCodeFrom(Reader embedJson) {
		BufferedReader reader = new BufferedReader(embedJson);
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				line = line.replaceAll("\\\\\"", "\"");
				line = line.substring(line.indexOf("<embed"));
				line = line.substring(0, line.indexOf("</embed>") + 8);
				line = line.replace("640", "384");
				line = line.replace("1280", "384");
				line = line.replace("390", "318");
				line = line.replace("750", "318");
				return line;
			}
			return null;
		} catch (IOException e) {
			throw new FetchException("Could not get embed code for blip.tv item", e);
		}
	}

	private String postsIdFrom(String videoSourceUri) {
		return videoSourceUri.substring(videoSourceUri.lastIndexOf("/") + 1);
	}

}
