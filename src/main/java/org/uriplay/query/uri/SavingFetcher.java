/* Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.uriplay.query.uri;

  import org.uriplay.media.entity.Content;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Playlist;
import org.uriplay.persistence.content.ContentWriter;
import org.uriplay.persistence.system.Fetcher;


/**
 * Decorator for a {@link Fetcher} that stores the results before passing them on.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class SavingFetcher implements Fetcher<Object> {

	private final Fetcher<Content> delegateFetcher;
	private final ContentWriter store;

	public SavingFetcher(Fetcher<Content> delegateFetcher, ContentWriter store) {
		this.delegateFetcher = delegateFetcher;
		this.store = store;
	}

	public Object fetch(String uri) {
		
		Content bean = delegateFetcher.fetch(uri);
		
		if (bean == null) { return null; }
		
		createOrUpdateContent(bean, false);
		
		return bean;
	}
	
    private void createOrUpdateContent(Content root, boolean markMissingItemsAsUnavailable) {
        if (root instanceof Playlist) {
            store.createOrUpdatePlaylist((Playlist) root, markMissingItemsAsUnavailable);
        }
        if (root instanceof Item) {
           store.createOrUpdateItem((Item) root);
        }
    }
}
