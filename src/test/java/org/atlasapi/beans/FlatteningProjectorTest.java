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

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;

import com.google.common.collect.Sets;

/**
 * Unit test for {@link FlatteningProjector}
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class FlatteningProjectorTest extends TestCase {

	public void testRemovesOuterPlaylistFromGraph() throws Exception {
		
		Playlist outerPlaylist = new Playlist();
		Playlist innerPlaylist = new Playlist();
		Episode ep1 = new Episode();
		Episode ep2 = new Episode();
		outerPlaylist.addItem(ep1);
		innerPlaylist.addItem(ep2);
		outerPlaylist.addPlaylist(innerPlaylist);
		
		Set<Object> beans = Sets.<Object>newHashSet(outerPlaylist, innerPlaylist, ep1, ep2);
		
		Collection<Object> projected = new FlatteningProjector().applyTo(beans);
		
		assertThat(projected, hasItem((Object) outerPlaylist));
		assertThat(projected, hasItems((Object) ep1, ep2));
		assertThat(projected, not(hasItem((Object) innerPlaylist)));
		
		assertThat(outerPlaylist.getItems(), hasItems((Item) ep1, ep2));
	}
	
	public void testLeavesSinglePlaylistUnaltered() throws Exception {
		
		Playlist playlist = new Playlist();
		Item ep = new Episode();
		playlist.addItem(ep);
		
		Set<Object> beans = Sets.newHashSet((Object) playlist, ep);
		
		Collection<Object> projected = new FlatteningProjector().applyTo(beans);
		
		assertThat(projected.size(), is(2));
		assertThat(projected, hasItem((Object) playlist));	
		assertThat(projected, hasItem((Object) ep));	
		assertThat(playlist.getItems(), hasItem(ep));
	}
	
	public void testCollapsesMultipleLevelsOfLists() throws Exception {
		
		Playlist playlist1 = new Playlist();
		Playlist playlist2 = new Playlist();
		playlist1.addPlaylist(playlist2);
		Playlist playlist3 = new Playlist();
		playlist2.addPlaylist(playlist3);
		Item ep = new Episode();
		playlist3.addItem(ep);
		
		Set<Object> beans = Sets.newHashSet((Object) playlist1, playlist2, playlist3, ep);
		
		Collection<Object> projected = new FlatteningProjector().applyTo(beans);
		
		assertThat(projected.size(), is(2));
		assertThat(projected, hasItem((Object) playlist1));	
		assertThat(projected, hasItem((Object) ep));	
		assertThat(playlist1.getItems(), hasItem(ep));
	}
	
	public void testTreatsBrandsAsPlaylists() throws Exception {
		
		Playlist playlist = new Playlist();
		Brand brand = new Brand();
		Item ep = new Episode();
		brand.addItem(ep);
		playlist.addPlaylist(brand);
		
		Set<Object> beans = Sets.newHashSet((Object) playlist, brand, ep);
		
		Collection<Object> projected = new FlatteningProjector().applyTo(beans);
		
		assertThat(projected.size(), is(2));
		assertThat(projected, hasItem((Object) playlist));	
		assertThat(projected, hasItem((Object) ep));	
		assertThat(projected, not(hasItem((Object) brand)));	
		assertThat(playlist.getItems(), hasItem(ep));
	}
	
	public void testThrowsExceptionIfGraphContainsNoOuterCollection() throws Exception {
		
		Item ep = new Episode();
		
		Set<Object> beans = Sets.newHashSet((Object) ep);
		
		try {
			new FlatteningProjector().applyTo(beans);
			fail("expected exception");
		} catch (Exception e) {
			assertThat(e, is(instanceOf(ProjectionException.class)));
			assertThat(e.getMessage(), containsString("No root collection found"));
		}
	}
	
	public void testThrowsExceptionIfGraphContainsMultipleOuterCollections() throws Exception {
		
		Playlist playlist1 = new Playlist();
		Playlist playlist2 = new Playlist();
		
		Set<Object> beans = Sets.newHashSet((Object) playlist1, playlist2);
		
		try {
			new FlatteningProjector().applyTo(beans);
			fail("expected exception");
		} catch (Exception e) {
			assertThat(e, is(instanceOf(ProjectionException.class)));
			assertThat(e.getMessage(), containsString("Multiple root elements"));
		}
	}
}
