package org.uriplay.query.uri.canonical;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uriplay.media.entity.Content;
import org.uriplay.persistence.content.ContentResolver;
import org.uriplay.persistence.content.mongo.AliasWriter;
import org.uriplay.persistence.system.Fetcher;
import org.uriplay.remotesite.NoMatchingAdapterException;

import com.google.common.collect.Sets;

public class CanonicalisingLocalRemoteFetcher implements Fetcher<Content>, ContentResolver {

	private static final int MAX_CANONICALISATIONS = 5;
	private final Log log = LogFactory.getLog(getClass());
	
	private final List<Canonicaliser> chain;
	private final Fetcher<Content> delegate;
	private final AliasWriter store;

	public CanonicalisingLocalRemoteFetcher(Fetcher<Content> delegate, List<Canonicaliser> chain, AliasWriter store) {
		this.delegate = delegate;
		this.chain = chain;
		this.store = store;
	}

	@Override
	public Content fetch(String uri) {
		Set<String> aliases = Sets.newHashSet();
		String currentUri = uri;
		for (int i = 0; i < MAX_CANONICALISATIONS; i++) {
			try {
				Content bean = delegate.fetch(currentUri);
				if (bean == null) {
					return null;
				} else {
					return saveAliases(bean, currentUri, aliases);
				}
			} catch (NoMatchingAdapterException e) {
				String nextUri = nextUri(currentUri, aliases);
				if (nextUri == null || nextUri.equals(currentUri)) {
					return null;
				}
				aliases.add(currentUri);
				currentUri = nextUri;
			}
		}
		return null;
	}

	private String nextUri(String currentUri, Set<String> aliases) {
		for (Canonicaliser canonicaliser : chain) {
			String next = canonicaliseOrNull(currentUri, canonicaliser);
			if (next != null) {
				return next;
			}
		}
		return null;
	}

	private String canonicaliseOrNull(String currentUri, Canonicaliser canonicaliser) {
		try {
			return canonicaliser.canonicalise(currentUri);
		} catch (Exception e) {
			log.warn(e);
			return null;
		}
	}

	private Content saveAliases(Content bean, String canonicalUri, Set<String> aliases) {
		if (!aliases.isEmpty()) {
			store.addAliases(canonicalUri, aliases);
		}
		return bean;
	}

	@Override
	public Content findByUri(String uri) {
		return (Content) fetch(uri);
	}
}
