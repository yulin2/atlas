package org.atlasapi.remotesite;

import static org.atlasapi.content.criteria.ContentQueryBuilder.query;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.content.criteria.ContentQueryBuilder;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.system.Fetcher;

/**
 * Updater to iterate through stored data for given publishers and refetch to update URIplay
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author John Ayres (john@metabroadcast.com)
 */
public class PerPublisherItemUpdater implements Runnable {

	private static Log LOG = LogFactory.getLog(PerPublisherItemUpdater.class);
	
	private final Fetcher<Content> uriplayFetcher;
	private final KnownTypeQueryExecutor contentStore;

	private Iterable<String> publishers;

	public PerPublisherItemUpdater(KnownTypeQueryExecutor contentStore, Fetcher<Content> fetcher) {
		this.contentStore = contentStore;
		this.uriplayFetcher = fetcher;
	}

	private void update(String publisher) {
		ContentQueryBuilder publisherEqualsQuery = query().equalTo(Attributes.ITEM_PUBLISHER, publisher);
		
		List<Item> items = contentStore.executeItemQuery(publisherEqualsQuery.build());
		
		for (Item item : items) {
			try {
				uriplayFetcher.fetch(item.getCanonicalUri());
				LOG.info("Updating info from:" + item.getCanonicalUri());
			} catch (Exception e) {
				LOG.warn((e));
				continue; // try the next uri
			}
		}
	}

	@Override
	public void run() {
		for (String publisher : publishers) {
			LOG.info("Updating for publisher: " + publisher);
			update(publisher);
		}
	}

	public void setUris(Iterable<String> publishers) {
		this.publishers = publishers;
	}

}
