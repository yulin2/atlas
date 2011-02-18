package org.atlasapi.remotesite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.Content;
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
	
	private final Fetcher<Content> fetcher;
	private final KnownTypeQueryExecutor contentStore;

	private Iterable<String> publishers;

	public PerPublisherItemUpdater(KnownTypeQueryExecutor contentStore, Fetcher<Content> fetcher) {
		this.contentStore = contentStore;
		this.fetcher = fetcher;
	}

	private void update(String publisher) {
		
		throw new UnsupportedOperationException("Do we need this?");
//		ContentQueryBuilder publisherEqualsQuery = query().equalTo(Attributes.DESCRIPTION_PUBLISHER, Publisher.fromKey(publisher).requireValue());
//		
//		List<Item> items = contentStore.executeItemQuery(publisherEqualsQuery.build());
//		
//		for (Item item : items) {
//			try {
//				fetcher.fetch(item.getCanonicalUri());
//				LOG.info("Updating info from:" + item.getCanonicalUri());
//			} catch (Exception e) {
//				LOG.warn((e));
//				continue; // try the next uri
//			}
//		}
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
