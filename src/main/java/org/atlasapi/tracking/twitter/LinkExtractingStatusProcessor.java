package org.atlasapi.tracking.twitter;

import java.util.Set;

import org.atlasapi.persistence.tracking.ContentMention;
import org.atlasapi.persistence.tracking.PossibleContentUriMentionListener;
import org.atlasapi.persistence.tracking.TrackingSource;
import org.atlasapi.util.text.MessageToSafeHtmlConverter;
import org.atlasapi.util.text.UrlExtractor;
import org.joda.time.DateTime;

import twitter4j.Status;

import com.google.common.collect.Sets;
import com.metabroadcast.common.social.buzz.twitter.TweetProcessor;

public class LinkExtractingStatusProcessor implements TweetProcessor {

	private final UrlExtractor urlExtractor = new MessageToSafeHtmlConverter();
	private final PossibleContentUriMentionListener listener;

	public LinkExtractingStatusProcessor(PossibleContentUriMentionListener listener) {
		this.listener = listener;
	}
	
	@Override
	public void process(Status status) {
		String statusText = status.getText();
		if (statusText == null) {
			// can't do anything
			return;
		}
		Set<String> urls = Sets.newHashSet(urlExtractor.extractUrls(statusText));
		
		DateTime createdAt = new DateTime(status.getCreatedAt());
		for (String url : urls) {
			listener.mentioned(new ContentMention(url, TrackingSource.TWITTER, String.valueOf(status.getId()), createdAt));
		}
	}
}
