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

package org.atlasapi.remotesite.hulu;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.feeds.OembedItem;
import org.atlasapi.query.content.PerPublisherCurieExpander;
import org.atlasapi.remotesite.oembed.OembedGraphExtractor;

import com.metabroadcast.common.media.MimeType;

/**
 * Specialisation of {@link OembedGraphExtractor} that pulls a location uri out of the embed code.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class HuluOembedGraphExtractor extends OembedGraphExtractor {

	private static Pattern locationPattern = Pattern.compile("value=\"(http://www.hulu.com/embed/.+?)\"");
	
	@Override
	protected String extractLocationUriFrom(OembedItem oembed) {
		String embedCode = oembed.embedCode();
		Matcher matcher = locationPattern.matcher(embedCode);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
	
	@Override
	protected MimeType getDataContainerFormat() {
		return MimeType.VIDEO_XFLV;
	}
	
	@Override
	protected String curieFor(String itemUri) {
		return PerPublisherCurieExpander.CurieAlgorithm.HULU.compact(itemUri);
	}
}
