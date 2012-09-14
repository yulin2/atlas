//package org.atlasapi.remotesite.youtube;
//
//import java.util.List;
//
//import org.atlasapi.media.content.Content;
//import org.atlasapi.media.entity.ContentGroup;
//import org.atlasapi.media.entity.Item;
//import org.atlasapi.persistence.content.ContentWriter;
//import org.atlasapi.persistence.logging.AdapterLog;
//import org.atlasapi.persistence.logging.AdapterLogEntry;
//import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
//
//import com.google.common.collect.ImmutableList;
//
//public class YouTubeFeedUpdater implements Runnable {
//
//	private final List<String> feedUris;
//	private final AdapterLog log;
//	private final YouTubeFeedAdapter client = new YouTubeFeedAdapter();
//	private final ContentWriter writer;
//	
//	public YouTubeFeedUpdater(Iterable<String> feedUris, AdapterLog log, ContentWriter writer) {
//		this.log = log;
//		this.writer = writer;
//		this.feedUris = ImmutableList.copyOf(feedUris);
//	}
//	
//	@Override
//	public void run() {
//		log.record(new AdapterLogEntry(Severity.DEBUG).withDescription("Starting YouTube feeds update"));
//		for (String uri : feedUris) {
//			try {
//				ContentGroup group = client.fetch(uri);
//				for (Content content : group.getContents()) {
//					writer.createOrUpdate((Item) content);
//				}
//				writer.createOrUpdateSkeleton(group);
//			} catch (Exception e) {
//				log.record(new AdapterLogEntry(Severity.ERROR).withCause(e));
//			}
//		}
//	}
//}
