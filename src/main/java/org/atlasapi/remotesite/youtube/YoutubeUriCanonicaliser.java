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

package org.atlasapi.remotesite.youtube;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.query.uri.canonical.Canonicaliser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class YoutubeUriCanonicaliser implements Canonicaliser {

    private static final Logger log = LoggerFactory.getLogger(YoutubeUriCanonicaliser.class);

	private static final List<Pattern> alternateUris = Lists.newArrayList(
        Pattern.compile("https?://.*\\.youtube.com.*v=([^\\./&=]+).*"),
        Pattern.compile("https?://.*\\.youtube.com/v/([^\\./&=]+).*"),
        Pattern.compile("http://.*\\.youtube.com/watch?v/([^\\./&=]+).*"),
		Pattern.compile("tag:youtube.com,\\d+:\\w+:([^\\./&=]+)")
    );

	public static String canonicalUriFor(String videoId) {
		return "http://gdata.youtube.com/feeds/api/videos/" + videoId;
	}

	public static String standardURL(String videoId){
	    return "http://www.youtube.com/watch?" + videoId;
	}

	public static String videoIdFrom(String uri) throws YouTubeException {
	    checkNotNull(uri);
        for (Pattern p : alternateUris) {
            Matcher matcher = p.matcher(uri);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        throw new YouTubeException(String.format("Provided URI %s deosn't match any known YouTube URI patterns", uri));
	}

	@Override
	public String canonicalise(String alternate) {
		try {
		    String programmeId = videoIdFrom(alternate);
            if (programmeId != null) {
                return canonicalUriFor(programmeId);
            }
        } catch (YouTubeException e) {
            log.error(e.getMessage(), e);
        }
		/**
		 * This should have been an Optional on the interface.
		 * Changing this to Optional now means changing way too much into
		 * the atlas codebase, that is completely unrelated to the YouTube
		 * Adapter itself, it this is to change, it deserves a task of its own.
		 */
		return null;
	}

	// Curie [ yt:abcd ]
	public static String curieFor(String uri) {
		String videoId = "";
        try {
            videoId = videoIdFrom(uri);
        } catch (YouTubeException e) {
            YoutubeUriCanonicaliser.log.error(e.getMessage(), e);
        }
		return "yt:" + videoId;
	}
}
