package org.uriplay.query.uri.canonical;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jherd.remotesite.Fetcher;
import org.jherd.remotesite.NoMatchingAdapterException;
import org.jherd.remotesite.timing.RequestTimer;
import org.uriplay.persistence.content.MutableContentStore;

import com.google.common.collect.Sets;

public class CanonicalisingLocalRemoteFetcher implements Fetcher<Set<Object>> {

	private static final int MAX_CANONICALISATIONS = 5;
	private final Log log = LogFactory.getLog(getClass());
	
	private final List<Canonicaliser> chain;
	private final Fetcher<Set<Object>> delegate;
	private final MutableContentStore store;

	public CanonicalisingLocalRemoteFetcher(Fetcher<Set<Object>> delegate, List<Canonicaliser> chain, MutableContentStore store) {
		this.delegate = delegate;
		this.chain = chain;
		this.store = store;
	}

	@Override
	public Set<Object> fetch(String uri, RequestTimer timer) {
		Set<String> aliases = Sets.newHashSet();
		String currentUri = uri;
		for (int i = 0; i < MAX_CANONICALISATIONS; i++) {
			try {
				Set<Object> beans = delegate.fetch(currentUri, timer);
				if (beans == null) {
					return null;
				} else {
					return saveAliases(beans, currentUri, aliases);
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

	private Set<Object> saveAliases(Set<Object> beans, String canonicalUri, Set<String> aliases) {
		if (!aliases.isEmpty()) {
			store.addAliases(canonicalUri, aliases);
		}
		return beans;
	}
}
