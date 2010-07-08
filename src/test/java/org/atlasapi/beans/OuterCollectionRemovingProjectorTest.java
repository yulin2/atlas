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

package org.atlasapi.beans;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.Collection;
import java.util.Set;

import junit.framework.TestCase;

import org.atlasapi.beans.OuterCollectionRemovingProjector;
import org.atlasapi.beans.ProjectionException;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;

import com.google.common.collect.Sets;

/**
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class OuterCollectionRemovingProjectorTest extends TestCase {

	public void testRemovesOuterPlaylistFromGraph() throws Exception {
		
		Playlist outerPlaylist = new Playlist();
		Playlist innerPlaylist = new Playlist();
		Episode ep1 = new Episode();
		Episode ep2 = new Episode();
		outerPlaylist.addItem(ep1);
		innerPlaylist.addItem(ep2);
		outerPlaylist.addPlaylist(innerPlaylist);
		
		Set<Object> beans = Sets.<Object>newHashSet(outerPlaylist, innerPlaylist, ep1, ep2);
		
		Collection<Object> projected = new OuterCollectionRemovingProjector().applyTo(beans);
		
		assertThat(projected, hasItems((Object) ep1, ep2, innerPlaylist));
		assertThat(projected, not(hasItem((Object) outerPlaylist)));
	}
	
	public void testThrowsExceptionIfGraphContainsNoOuterCollection() throws Exception {
		
		Item ep = new Episode();
		
		Set<Object> beans = Sets.newHashSet((Object) ep);
		
		try {
			new OuterCollectionRemovingProjector().applyTo(beans);
			fail("expected exception");
		} catch (Exception e) {
			assertThat(e, is(instanceOf(ProjectionException.class)));
			assertThat(e.getMessage(), containsString("No collections found in object graph"));
		}
	}
}
