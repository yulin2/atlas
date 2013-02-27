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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.query.uri.canonical.Canonicaliser;

import com.google.common.collect.Lists;

public class YoutubeUriCanonicaliser implements Canonicaliser {

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

	public static String videoIdFrom(String uri) {
	    if(uri != null){
    		for (Pattern p : alternateUris) {
    			Matcher matcher = p.matcher(uri);
    			if (matcher.matches()) {
    				return matcher.group(1);
    			}
    		}
    	}
		return null;
	}

	@Override
	public String canonicalise(String alternate) {
		String programmeId = videoIdFrom(alternate);
		if (programmeId == null) {
			return null;
		}
		return canonicalUriFor(programmeId);
	}

	// Curie [ yt:abcd ]
	public static String curieFor(String uri) {
		String videoId = videoIdFrom(uri);
		return "yt:" + videoId;
	}
}
