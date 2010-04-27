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

package org.uriplay.beans;
import java.util.Iterator;
import java.util.Set;

import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Playlist;
import org.uriplay.media.entity.Version;
import org.uriplay.query.uri.Profile;

import com.google.common.collect.Sets;

/**
 * A {@link Filter} that removes beans from the bean graph dependent
 * on the specified {@link Profile}.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class ProfileFilter implements Filter {

	public Set<Object> applyTo(Set<Object> beans, Profile profile) {
		
		if (profile == Profile.ALL) {
			return beans;
		}
		
		Set<Object> filtered = Sets.newHashSet();
		
		for (Iterator<Object> i = beans.iterator(); i.hasNext(); ) {
			
			Object bean = i.next();

			if (bean instanceof Playlist) {
				
				Playlist playlist = (Playlist) bean;
				
				if (shouldInclude(playlist, profile)) {
					filtered.add(playlist);
				}
			}
			
			if (bean instanceof Item) {
				
				Item item = (Item) bean;
				
				if (shouldInclude(item, profile)) {
					filtered.add(item);
				}
			}
		}
		
		return filtered;
	}

	private boolean shouldInclude(Playlist playlist, Profile profile) {
		
		for (Iterator<Item> i = playlist.getItems().iterator(); i.hasNext(); ) {
			if (!shouldInclude(i.next(), profile)) {
				i.remove();
			}
		}
		
		for (Iterator<Playlist> i = playlist.getPlaylists().iterator(); i.hasNext(); ) {
			if (!shouldInclude(i.next(), profile)) {
				i.remove();
			}
		}
		
		return !playlist.getItems().isEmpty() || !playlist.getPlaylists().isEmpty();
	}

	private boolean shouldInclude(Item item, Profile profile) {
		
		for (Iterator<Version> i = item.getVersions().iterator(); i.hasNext(); ) {
			if (!shouldInclude(i.next(), profile)) {
				i.remove();
			}
		}
		
		return !item.getVersions().isEmpty();
	}

	private boolean shouldInclude(Version version, Profile profile) {
		
		for (Iterator<Encoding> i = version.getManifestedAs().iterator(); i.hasNext(); ) {
			if (!shouldInclude(i.next(), profile)) {
				i.remove();
			}
		}
		
		return !version.getManifestedAs().isEmpty();
	}

	private boolean shouldInclude(Encoding encoding, Profile profile) {
		
		for (Iterator<Location> i = encoding.getAvailableAt().iterator(); i.hasNext(); ) {
			if (!profile.matches(i.next())) {
				i.remove();
			}
		}
		
		return profile.matches(encoding);
	}

}
