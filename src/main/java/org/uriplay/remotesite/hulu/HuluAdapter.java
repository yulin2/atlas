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

import javax.xml.bind.JAXBException;

import org.uriplay.query.uri.canonical.Canonicaliser;
import org.uriplay.remotesite.oembed.OembedXmlAdapter;
import org.uriplay.remotesite.oembed.OembedXmlClient;

public class HuluAdapter extends OembedXmlAdapter {

	private static final String BASE_URI = "http://www.hulu.com/watch/";
	private static final Pattern ALIAS_PATTERN = Pattern.compile("(" + BASE_URI + "[^/&\\?=@]+).*");
	
	public HuluAdapter() throws JAXBException {
		super(new OembedXmlClient(), new HuluOembedGraphExtractor());
		setAcceptedUriPattern(BASE_URI + "[^/&\\?=@]+");
		setOembedEndpoint("http://www.hulu.com/api/oembed.xml");
		setPublisher("hulu.com");
	}
	
	public static class HuluCanonicaliser implements Canonicaliser {

		@Override
		public String canonicalise(String uri) {
			Matcher matcher = ALIAS_PATTERN.matcher(uri);
			if (matcher.matches()) {
				return matcher.group(1);
			}
			return null;
		}
	}
}
