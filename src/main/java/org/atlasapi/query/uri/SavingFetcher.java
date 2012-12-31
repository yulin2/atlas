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

package org.atlasapi.query.uri;

  import org.atlasapi.media.content.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.system.Fetcher;


/**
 * Decorator for a {@link Fetcher} that stores the results before passing them on.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class SavingFetcher implements Fetcher<Identified> {

	private final Fetcher<Identified> delegateFetcher;
	private ContentWriter store;

	public SavingFetcher(Fetcher<Identified> delegateFetcher, ContentWriter store) {
		this.delegateFetcher = delegateFetcher;
		this.store = store;
	}
	
	public void setStore(ContentWriter store) {
		this.store = store;
	}

	public Identified fetch(String uri) {
		
		if (store == null) {
			throw new IllegalStateException();
		}
		
		Identified bean = delegateFetcher.fetch(uri);
		
		if (bean == null) { return null; }
		
		createOrUpdateContent(bean, false);
		
		return bean;
	}
	
    private void createOrUpdateContent(Identified root, boolean markMissingItemsAsUnavailable) {
        if (root instanceof Container) {
            store.createOrUpdate((Container) root);
        }
        if (root instanceof Item) {
           store.createOrUpdate((Item) root);
        }
    }
}
