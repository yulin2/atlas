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

package org.uriplay.remotesite.hulu;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.id.IdGeneratorFactory;
import org.uriplay.feeds.OembedItem;
import org.uriplay.query.content.PerPublisherCurieExpander;
import org.uriplay.remotesite.oembed.OembedGraphExtractor;
import org.uriplay.remotesite.oembed.OembedSource;

/**
 * Specialisation of {@link OembedGraphExtractor} that pulls a location uri out of the embed code.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class HuluOembedGraphExtractor extends OembedGraphExtractor implements BeanGraphExtractor<OembedSource> {

	private static Pattern locationPattern = Pattern.compile("value=\"(http://www.hulu.com/embed/.+?)\"");

	public HuluOembedGraphExtractor(IdGeneratorFactory idGeneratorFactory) {
		super(idGeneratorFactory);
	}
	
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
	protected String getDataContainerFormat() {
		return "video/x-flv";
	}
	
	@Override
	protected String curieFor(String itemUri) {
		return PerPublisherCurieExpander.CurieAlgorithm.HULU.compact(itemUri);
	}
}
