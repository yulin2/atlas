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

package org.uriplay.remotesite;

import java.util.Set;

import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Playlist;
import org.uriplay.persistence.content.ContentWriter;

import com.google.common.collect.Sets;

public class ContentWriters implements ContentWriter {

	private Set<ContentWriter> writers = Sets.newHashSet();
	
	@Override
	public void createOrUpdateItem(Item item) {
		for (ContentWriter writer : writers) {
			writer.createOrUpdateItem(item);
		}
	}

	@Override
	public void createOrUpdatePlaylist(Playlist enclosingList, boolean markMissingItemsAsUnavailable) {
		for (ContentWriter writer : writers) {
			writer.createOrUpdatePlaylist(enclosingList, markMissingItemsAsUnavailable);
		}
	}
	
	public void add(ContentWriter writer) {
		this.writers.add(writer);
	}
}
