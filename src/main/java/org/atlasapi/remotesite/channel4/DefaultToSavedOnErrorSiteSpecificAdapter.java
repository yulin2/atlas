package org.atlasapi.remotesite.channel4;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.remotesite.SiteSpecificAdapter;

public class DefaultToSavedOnErrorSiteSpecificAdapter<T> implements SiteSpecificAdapter<T> {

	private final ContentResolver contentStore;
	private final SiteSpecificAdapter<T> delegate;
	private final AdapterLog log;

	public DefaultToSavedOnErrorSiteSpecificAdapter(SiteSpecificAdapter<T> delegate, ContentResolver contentStore, AdapterLog log) {
		this.delegate = delegate;
		this.contentStore = contentStore;
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
			log.record(new AdapterLogEntry().withCause(e).withUri(uri).withSource(delegate.getClass()));
			return (T) contentStore.findByUri(uri);
		}
	}
}
