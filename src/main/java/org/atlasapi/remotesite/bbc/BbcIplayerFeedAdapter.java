/* Copyright 2009 British Broadcasting Corporation
   Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.remotesite.bbc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Playlist;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.synd.SyndicationAdapter;
import org.atlasapi.remotesite.synd.SyndicationFeedClient;
import org.atlasapi.remotesite.synd.SyndicationSource;

import com.metabroadcast.common.base.Maybe;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * {@link SiteSpecificAdapter} processing iPlayer's RSS feeds.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author John Ayres (john@metabroadcast.com)
 */
public class BbcIplayerFeedAdapter extends SyndicationAdapter<Playlist> implements SiteSpecificAdapter<Playlist> {

	private static final String BBC_ATOZ_CURIE_PREFIX = "bbc:atoz_";
	private static final String BBC_HIGHLIGHTS_CURIE_BASE = "bbc:highlights_";
	private static  final String BBC_POPULAR_CURIE_BASE = "bbc:popular_";
	private static  final String BBC_CHANNEL_CURIE_PREFIX = "bbc:bbc_";
	
	private static final Pattern atozUriPattern = Pattern.compile("http://feeds.bbc.co.uk/iplayer/atoz/([a-z]|0-9)/list");
	private static final Pattern CHANNEL_URI_PATTERN = Pattern.compile("http://feeds.bbc.co.uk/iplayer/bbc_([^/]+)/list");
	private static final Pattern highlightsFeedPattern = Pattern.compile("http://feeds.bbc.co.uk/iplayer/(popular|highlights)/(tv|radio)");
	
	public BbcIplayerFeedAdapter() {
		this(new SyndicationFeedClient(), new BbcIplayerGraphExtractor());
	}
	
	protected BbcIplayerFeedAdapter(RemoteSiteClient<SyndFeed> feedClient, ContentExtractor<SyndicationSource, Playlist> contentExtractor) {
		super(feedClient, contentExtractor);
	}
	
	public boolean canFetch(String uri) {
		return atozUriPattern.matcher(uri).matches() || highlightsFeedPattern.matcher(uri).matches() || CHANNEL_URI_PATTERN.matcher(uri).matches();
	}

	public static Maybe<String> expand(String curie) {
		if (curie.startsWith(BBC_ATOZ_CURIE_PREFIX)) {
			return Maybe.just("http://feeds.bbc.co.uk/iplayer/atoz/" + curie.substring(BBC_ATOZ_CURIE_PREFIX.length()) + "/list");
		}
		if (curie.startsWith(BBC_HIGHLIGHTS_CURIE_BASE)) {
			return Maybe.just("http://feeds.bbc.co.uk/iplayer/highlights/" + curie.substring(BBC_HIGHLIGHTS_CURIE_BASE.length()));
		}
		if (curie.startsWith(BBC_POPULAR_CURIE_BASE)) {
			return Maybe.just("http://feeds.bbc.co.uk/iplayer/popular/" + curie.substring(BBC_POPULAR_CURIE_BASE.length()));
		}
		if (curie.startsWith(BBC_CHANNEL_CURIE_PREFIX)) {
			return Maybe.just("http://feeds.bbc.co.uk/iplayer/bbc_" + curie.substring(BBC_CHANNEL_CURIE_PREFIX.length()) + "/list");
		}
		return Maybe.nothing();
	}

	public static Maybe<String> compact(String url) {
		Matcher atozMatcher = BbcIplayerFeedAdapter.atozUriPattern.matcher(url);
		if (atozMatcher.matches()) {
			return Maybe.just(BBC_ATOZ_CURIE_PREFIX + atozMatcher.group(1));
		}
		Matcher hightlightsMatcher = BbcIplayerFeedAdapter.highlightsFeedPattern.matcher(url);
		if (hightlightsMatcher.matches()) {
			return Maybe.just("bbc:" + hightlightsMatcher.group(1) + "_" + hightlightsMatcher.group(2));
		}
		Matcher channelMatcher = CHANNEL_URI_PATTERN.matcher(url);
		if (channelMatcher.matches()) {
			return Maybe.just(BBC_CHANNEL_CURIE_PREFIX + channelMatcher.group(1));
		}
		
		return Maybe.nothing();
	}
}
