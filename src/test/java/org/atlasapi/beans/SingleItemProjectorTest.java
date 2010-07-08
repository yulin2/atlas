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
import static org.hamcrest.Matchers.is;

import java.util.Collection;
import java.util.Set;

import junit.framework.TestCase;

import org.atlasapi.beans.ProjectionException;
import org.atlasapi.beans.Projector;
import org.atlasapi.beans.SingleItemProjector;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Version;

import com.google.common.collect.Sets;

/**
 * Unit test for {@link SingleItemProjector}
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class SingleItemProjectorTest extends TestCase {

	Projector projector = new SingleItemProjector();
	
	public void testLeavesSingleItemUnaltered() throws Exception {
		
		Object item = new Item();
		
		Set<Object> beans = Sets.newHashSet(item);
		
		Collection<Object> projected = projector.applyTo(beans);
		
		assertThat(projected.size(), is(1));
		assertThat(projected, hasItem(item));
	}
	
	public void testLeavesSingleEpisodeUnaltered() throws Exception {
		
		Object episode = new Episode();
		
		Set<Object> beans = Sets.newHashSet(episode);
		
		Collection<Object> projected = projector.applyTo(beans);
		
		assertThat(projected.size(), is(1));
		assertThat(projected, hasItem(episode));
	}
	
	public void testThrowsExceptionIfGraphIsPlaylist() throws Exception {
		
		Object playlist = new Playlist();
		Set<Object> beans = Sets.newHashSet(playlist);
		
		try {
			projector.applyTo(beans);
			fail("expected exception");
		} catch (ProjectionException pe) {
			assertThat(pe.getMessage(), containsString("Not a single item"));
		}
	}
	
	public void testThrowsExceptionIfGraphContainsMultipleItems() throws Exception {
		
		Object ep1 = new Episode();
		Object ep2 = new Episode();
		Set<Object> beans = Sets.newHashSet(ep1, ep2);
		
		try {
			projector.applyTo(beans);
			fail("expected exception");
		} catch (ProjectionException pe) {
			assertThat(pe.getMessage(), containsString("Not a single item"));
		}
	}
	
	public void testLeavesSingleEpisodeVersionEncodingLocationTreeUnaltered() throws Exception {
		
		Episode episode = new Episode();
		Version version = new Version();
		Encoding encoding = new Encoding();
		Location location1 = new Location();
		Location location2 = new Location();
		
		episode.addVersion(version);
		version.addManifestedAs(encoding);
		encoding.addAvailableAt(location1);
		encoding.addAvailableAt(location2);
		
		Collection<Object> beans = Sets.newHashSet((Object) episode, version, encoding, location1, location2);
		
		Collection<Object> projected = projector.applyTo(beans);
		
		assertThat(projected.size(), is(5));
		assertThat(projected, hasItems((Object) episode, version, encoding, location1, location2));
	}
	
}
