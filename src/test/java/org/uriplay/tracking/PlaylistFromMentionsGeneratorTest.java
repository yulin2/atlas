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

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.joda.time.DateTime;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Playlist;
import org.uriplay.persistence.content.MutableContentStore;
import org.uriplay.persistence.testing.DummyContentData;
import org.uriplay.persistence.tracking.ContentMention;
import org.uriplay.persistence.tracking.ContentMentionStore;
import org.uriplay.persistence.tracking.TrackingSource;

import com.google.common.collect.Lists;

public class PlaylistFromMentionsGeneratorTest extends MockObjectTestCase {

	MutableContentStore contentStore;
	ContentMentionStore mentionStore;
	DummyContentData data;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.contentStore = mock(MutableContentStore.class);
		this.mentionStore = mock(ContentMentionStore.class);
		this.data = new DummyContentData();
	}
	
	public void testGeneratingPlaylists() throws Exception {
		PlaylistFromMentionsGenerator generator = new PlaylistFromMentionsGenerator(contentStore, mentionStore, TrackingSource.TWITTER);
		
		final List<ContentMention> mentions = Lists.newArrayList(mention("/a", TrackingSource.TWITTER), mention("/b", TrackingSource.TWITTER), mention("/b", TrackingSource.TWITTER));
		
		final Playlist expectedHotnessPlaylist = playlist("http://uriplay.org/hotness/twitter", "Hot on Twitter", data.englishForCats, data.dotCottonsBigAdventure);
		final Playlist expectedMentionsPlaylist = playlist("http://uriplay.org/mentions/twitter", "Mentioned on Twitter", data.dotCottonsBigAdventure, data.englishForCats, data.englishForCats);
		
		checking(new Expectations() {{
			one(mentionStore).mentions(TrackingSource.TWITTER, 10000); will(returnValue(mentions));
		
			allowing(contentStore).findByUri("/a"); will(returnValue(data.dotCottonsBigAdventure));
			allowing(contentStore).findByUri("/b"); will(returnValue(data.englishForCats));
		
			one(contentStore).createOrUpdatePlaylist(with(playlistEq(expectedHotnessPlaylist)), with(false));
			one(contentStore).createOrUpdatePlaylist(with(playlistEq(expectedMentionsPlaylist)), with(false));
		}});
		
		generator.run();
	}

	private Playlist playlist(String uri, String title, Item... items) {
		Playlist playlist = new Playlist();
		playlist.setCanonicalUri(uri);
		playlist.setTitle(title);
		playlist.addItems(items);
		return playlist;
	}

	protected Matcher<Playlist> playlistEq(final Playlist expected) {
		return new TypeSafeMatcher<Playlist>() {

			@Override
			public boolean matchesSafely(Playlist actual) {
				if (!actual.getCanonicalUri().equals(expected.getCanonicalUri())) {
					return false;
				}
				return expected.getItems().equals(actual.getItems()) && expected.getPlaylists().equals(actual.getPlaylists());
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("Playlist equal to " + expected.toString());
			}
		};
	}

	private ContentMention mention(String uri, TrackingSource source) {
		return new ContentMention(uri, source, String.valueOf(Math.random()), new DateTime());
	}
}
