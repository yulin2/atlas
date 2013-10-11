package org.atlasapi.remotesite.channel4.pmlsd;

import java.net.URL;

import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.WireFeedInput;
import com.sun.syndication.io.XmlReader;

public class AtomFeedBuilder {

	private final URL url;

	public AtomFeedBuilder(URL url) {
		this.url = url;
	}
	
	public Feed build() {
		try {
			return (Feed) new WireFeedInput().build(new XmlReader(url));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
