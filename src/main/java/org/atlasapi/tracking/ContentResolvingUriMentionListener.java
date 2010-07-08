package org.atlasapi.tracking;

import org.atlasapi.media.entity.Description;
import org.atlasapi.persistence.system.Fetcher;
import org.atlasapi.persistence.tracking.ContentMention;
import org.atlasapi.persistence.tracking.PossibleContentUriMentionListener;

public class ContentResolvingUriMentionListener implements PossibleContentUriMentionListener {

	private final Fetcher<Description> fetcher;
	private final PossibleContentUriMentionListener listener;

	public ContentResolvingUriMentionListener(Fetcher<Description> fetcher, PossibleContentUriMentionListener listener) {
		this.fetcher = fetcher;
		this.listener = listener;
	}
	
	@Override
	public void mentioned(ContentMention mention) {
		Description description = fetcher.fetch(mention.uri());
		if (description != null) {
			listener.mentioned(canonicalise(mention, description));
		}
	}

	private ContentMention canonicalise(ContentMention mention, Description description) {
		return new ContentMention(description.getCanonicalUri(), mention.source(), mention.externalRef(), mention.mentionedAt());
	}
	
	
}
