package org.atlasapi.remotesite.tinyurl;

import org.atlasapi.media.content.Identified;
import org.atlasapi.persistence.shorturls.ShortUrlSaver;
import org.atlasapi.query.uri.canonical.InterestedInTheEventualContentCanonicaliser;

public class SavingShortUrlCanonicaliser implements InterestedInTheEventualContentCanonicaliser {

	private final ShortenedUrlCanonicaliser shortenedUrlCanonicaliser;
	private final ShortUrlSaver saver;

	public SavingShortUrlCanonicaliser(ShortenedUrlCanonicaliser shortenedUrlCanonicaliser, ShortUrlSaver saver) {
		this.shortenedUrlCanonicaliser = shortenedUrlCanonicaliser;
		this.saver = saver;
	}
	
	@Override
	public void resolvedTo(String url, Identified resolvedTo) {
		saver.save(url, resolvedTo);
	}

	@Override
	public String canonicalise(String uri) {
		return shortenedUrlCanonicaliser.canonicalise(uri);
	}
}
