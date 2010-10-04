package org.atlasapi.remotesite.channel4;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.SiteSpecificAdapter;

public class DefaultToSavedOnErrorSiteSpecificAdapter<T> implements SiteSpecificAdapter<T> {

	private final ContentResolver contentStore;
	private final SiteSpecificAdapter<T> delegate;
	private final AdapterLog log;
	private final Publisher publisher;

	public DefaultToSavedOnErrorSiteSpecificAdapter(SiteSpecificAdapter<T> delegate, ContentResolver contentStore, Publisher publisher, AdapterLog log) {
		this.delegate = delegate;
		this.contentStore = contentStore;
		this.publisher = publisher;
		this.log = log;
	}
	
	@Override
	public boolean canFetch(String uri) {
		return delegate.canFetch(uri);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T fetch(String uri) {
		try {
			return delegate.fetch(uri);
		} catch (Exception e) {
			log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withUri(uri).withSource(delegate.getClass()));
			Content found = contentStore.findByUri(uri);
			if (publisher.equals(found.getPublisher())) {
				return (T) found;
			}
			return null;
		}
	}
}
