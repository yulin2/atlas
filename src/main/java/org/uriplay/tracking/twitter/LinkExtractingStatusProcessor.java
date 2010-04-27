package org.uriplay.tracking.twitter;

import java.util.Set;

import org.jherd.util.text.MessageToSafeHtmlConverter;
import org.jherd.util.text.UrlExtractor;
import org.joda.time.DateTime;
import org.uriplay.persistence.tracking.ContentMention;
import org.uriplay.persistence.tracking.PossibleContentUriMentionListener;
import org.uriplay.persistence.tracking.TrackingSource;

import twitter4j.Status;

import com.google.soy.common.collect.Sets;

public class LinkExtractingStatusProcessor implements StatusProcessor {

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
