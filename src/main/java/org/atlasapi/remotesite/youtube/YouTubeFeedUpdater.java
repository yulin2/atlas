package org.atlasapi.remotesite.youtube;

import java.util.List;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;

public class YouTubeFeedUpdater implements Runnable {

	private final List<String> feedUris;
	private final AdapterLog log;
	private final YouTubeFeedAdapter client = new YouTubeFeedAdapter();
    private final ContentResolver resolver;
	private final ContentWriter writer;
	
	public YouTubeFeedUpdater(Iterable<String> feedUris, AdapterLog log, ContentWriter writer, ContentResolver resolver) {
		this.log = log;
		this.writer = writer;
		this.feedUris = ImmutableList.copyOf(feedUris);
		this.resolver = resolver;
	}
	
	@Override
	public void run() {
		log.record(new AdapterLogEntry(Severity.DEBUG).withDescription("Starting YouTube feeds update"));
		for (String uri : feedUris) {
			try {
		        // look for existing item, merge into latest remote data, else fetch complete.
//                ContentGroup group = client.fetch(uri);
//                for (ChildRef content : group.getContents()) {
//    		        Maybe<Identified> possibleIdentified = resolver.findByCanonicalUris(ImmutableList.of(itemUri)).get(itemUri);
//    		        if (possibleIdentified.hasValue()) {
//    		            Identified ided = possibleIdentified.requireValue();
//                        writer.createOrUpdate(ided);
//    		        } else {
//    		            item = fetchFullItem(itemUri).or(basicItem);
//    		        }
//				}
//				writer.createOrUpdateSkeleton(group);
			} catch (Exception e) {
				log.record(new AdapterLogEntry(Severity.ERROR).withCause(e));
			}
		}
	}
}
