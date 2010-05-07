/* Copyright 2010 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.uriplay.tracking;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jherd.util.stats.Count;
import org.uriplay.media.entity.Description;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Playlist;
import org.uriplay.persistence.content.MutableContentStore;
import org.uriplay.persistence.tracking.ContentMention;
import org.uriplay.persistence.tracking.ContentMentionStore;
import org.uriplay.persistence.tracking.TrackingSource;

import com.google.soy.common.collect.Lists;
import com.google.soy.common.collect.Maps;

public class PlaylistFromMentionsGenerator implements Runnable {

	private static final int LIMIT = 10000;
	private static final String BASE_URI = "http://uriplay.org";

	private final Log log = LogFactory.getLog(getClass());
	
	private static final Comparator<Description> CANONICAL_URI_COMPARATOR = new Comparator<Description>() {

		@Override
		public int compare(Description d1, Description d2) {
			return d1.getCanonicalUri().compareTo(d1.getCanonicalUri());
		}
	};

	private final MutableContentStore content;
	private final ContentMentionStore mentionStore;
	
	private TrackingSource source;

	public PlaylistFromMentionsGenerator(MutableContentStore content, ContentMentionStore mentions, TrackingSource source) {
		this.mentionStore = mentions;
		this.content = content;
		this.source = source;
	}

	@Override
	public void run() {
		log.info("Updating hotness and mentions playlists for " + source);
		generateAndSavePlaylists(mentionStore.mentions(source, LIMIT));
		log.info("Completed updating hotness and mentions playlists for " + source);
	}

	private void generateAndSavePlaylists(List<ContentMention> mentioned) {
		
		Playlist mentionsPlaylist = playlist("mentions", "Mentions on ");
		Map<Item, Count<Item>> itemMentionCounts = Maps.newHashMap();
		Map<Playlist, Count<Playlist>> playlistMentionCounts = Maps.newHashMap();
		
		for (ContentMention contentMention : mentioned) {
			Description found = content.findByUri(contentMention.uri());
			if (found != null) {
				if (found instanceof Item) {
					Item item = (Item) found;
					if (item.isAvailable() && item.getIsLongForm()) {
						mentionsPlaylist.addItem(item);
						incrementCount(itemMentionCounts, item);
					}
				}
				if (found instanceof Playlist) {
					mentionsPlaylist.addPlaylist((Playlist) found);
					incrementCount(playlistMentionCounts, (Playlist) found);
				}
			}
		}
		
		content.createOrUpdatePlaylist(mentionsPlaylist, false);
		content.createOrUpdatePlaylist(hotnessPlaylist(itemMentionCounts, playlistMentionCounts), false);
	}
	
	/**
	 * TODO {@link Playlist}s currently contain {@link Set}s of sub items and playlists.  These should be
	 * converted into {@link List}s so that ordering is preserved.
	 */
	private Playlist hotnessPlaylist(Map<Item, Count<Item>> itemMentionCounts, Map<Playlist, Count<Playlist>> playlistMentionCounts) {
		Playlist hotnessPlaylist = playlist("hotness", "Hot on ");
		
		List<Count<Item>> hotItems = Lists.newArrayList(itemMentionCounts.values());
		List<Count<Playlist>> hotPlaylists = Lists.newArrayList(playlistMentionCounts.values());
		
		Collections.sort(hotItems, Collections.reverseOrder());
		Collections.sort(hotPlaylists, Collections.reverseOrder());
		
		hotnessPlaylist.setItems(targetsFrom(hotItems));
		hotnessPlaylist.setPlaylists(targetsFrom(hotPlaylists));
		
		return hotnessPlaylist;
	}

	private <T> List<T> targetsFrom(List<Count<T>> hot) {
		List<T> targets = Lists.newArrayList();
		for (Count<T> count : hot) {
			targets.add(count.getTarget());
		}
		return targets;
	}

	private Playlist playlist(String uriFragment, String titlePrefix) {
		Playlist mentionsPlaylist = new Playlist();
		mentionsPlaylist.setCanonicalUri(BASE_URI +  "/" + uriFragment+  "/" + source.uriFragment());
		mentionsPlaylist.setTitle(titlePrefix + source.title());
		return mentionsPlaylist;
	}

	private <T extends Description> void incrementCount(Map<T, Count<T>> counts, T target) {
		Count<T> count = counts.get(target);
		if (count == null) {
			count = Count.of(target, CANONICAL_URI_COMPARATOR);
			counts.put(target, count);
		}
		count.increment();
	}	
}
