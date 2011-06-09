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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.synd.SyndicationFeedClient;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.metabroadcast.common.base.Maybe;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndLink;

/**
 * {@link SiteSpecificAdapter} processing iPlayer's RSS feeds.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author John Ayres (john@metabroadcast.com)
 */
public class BbcIplayerHightlightsAdapter implements Runnable {

	private static final String BBC_ATOZ_CURIE_PREFIX = "bbc:atoz_";
	private static final String BBC_HIGHLIGHTS_CURIE_BASE = "bbc:highlights_";
	private static  final String BBC_POPULAR_CURIE_BASE = "bbc:popular_";
	private static  final String BBC_CHANNEL_CURIE_PREFIX = "bbc:bbc_";
	
	private static final Pattern atozUriPattern = Pattern.compile("http://feeds.bbc.co.uk/iplayer/atoz/([a-z]|0-9)/list");
	private static final Pattern CHANNEL_URI_PATTERN = Pattern.compile("http://feeds.bbc.co.uk/iplayer/bbc_([^/]+)/list");
	
	private static final Pattern highlightsFeedPattern = Pattern.compile("http://feeds.bbc.co.uk/iplayer/(popular|highlights)/(tv|radio)");
	
	private final AdapterLog log;
	private final RemoteSiteClient<SyndFeed> feedClient;
	private final ContentWriter writer;
	
	public BbcIplayerHightlightsAdapter(ContentWriter writer, AdapterLog log) {
		this(new SyndicationFeedClient(), log, writer);
	}
	
	protected BbcIplayerHightlightsAdapter(RemoteSiteClient<SyndFeed> feedClient, AdapterLog log, ContentWriter writer) {
		this.feedClient = feedClient;
		this.log = log;
		this.writer = writer;
	}
	
	@Override
	public void run() {
		for (String type:  ImmutableList.of("popular", "highlights")) {
			for (String media : ImmutableList.of("tv", "radio")) {
				String uri = "http://feeds.bbc.co.uk/iplayer/" + type + "/" + media;
				try {
					loadAndSave(uri);
				} catch (Exception e) {
					log.record(new AdapterLogEntry(Severity.WARN).withUri(uri).withCause(e).withSource(getClass()));
				}
			}
		}
	}

	private void loadAndSave(String uri) throws Exception {
		List<String> urisFrom = readUrisFrom(uri);
		ContentGroup playlist = new ContentGroup(uri, compact(uri).requireValue(), Publisher.BBC);
		playlist.setContentUris(urisFrom);
		writer.createOrUpdateSkeleton(playlist);
	}

	@SuppressWarnings("unchecked")
	private List<String> readUrisFrom(String feedUri) throws Exception {
		SyndFeed feed = feedClient.get(feedUri);
		List<String> uris = Lists.newArrayList();
		for (SyndEntry entry : (List<SyndEntry>) feed.getEntries()) {
			String uri = extractUriFrom(entry);
			if (uri != null) {
				uris.add(uri);
			} else {
				log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withDescription("Could not extract BBC uri with PID from feed entry: " + entry));
			}
		}
		return uris;
	}
	
	private String extractUriFrom(SyndEntry entry) {
		String selfLink = selfLink(entry);
		if (selfLink == null) {
			return null;
		}
		return BbcFeeds.slashProgrammesUriForPid(BbcFeeds.pidFrom(selfLink));
	}

	@SuppressWarnings("unchecked")
	private String selfLink(SyndEntry entry) {
		for (SyndLink link : (List<SyndLink>) entry.getLinks()) {
			if ("self".equals(link.getRel())) {
				return link.getHref();
			}
		}
		return null;
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
		Matcher atozMatcher = BbcIplayerHightlightsAdapter.atozUriPattern.matcher(url);
		if (atozMatcher.matches()) {
			return Maybe.just(BBC_ATOZ_CURIE_PREFIX + atozMatcher.group(1));
		}
		Matcher hightlightsMatcher = BbcIplayerHightlightsAdapter.highlightsFeedPattern.matcher(url);
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
