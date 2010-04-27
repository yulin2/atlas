package org.uriplay.tracking;

import java.util.Set;

import org.jherd.remotesite.Fetcher;
import org.jherd.remotesite.timing.NullRequestTimer;
import org.uriplay.media.entity.Description;
import org.uriplay.persistence.tracking.ContentMention;
import org.uriplay.persistence.tracking.PossibleContentUriMentionListener;

public class ContentResolvingUriMentionListener implements PossibleContentUriMentionListener {

	private final Fetcher<Set<Description>> fetcher;
	private final PossibleContentUriMentionListener listener;

	public ContentResolvingUriMentionListener(Fetcher<Set<Description>> fetcher, PossibleContentUriMentionListener listener) {
		this.fetcher = fetcher;
		this.listener = listener;
	}
	
	@Override
	public void mentioned(ContentMention mention) {
		Description description = findDescriptionFrom(mention.uri(), fetcher.fetch(mention.uri(), new NullRequestTimer()));
		if (description != null) {
			listener.mentioned(canonicalise(mention, description));
		}
	}

	private ContentMention canonicalise(ContentMention mention, Description description) {
		return new ContentMention(description.getCanonicalUri(), mention.source(), mention.externalRef(), mention.mentionedAt());
	}
	
	private Description findDescriptionFrom(String uri, Set<? extends Description> descriptions) {
		if (descriptions == null) {
			return null;
		}
		for (Description description : descriptions) {
			Set<String> uris = description.getAllUris();
			if (uris != null && uris.contains(uri)) {
				return description;
			}
 		}
		return null;
	}
}
