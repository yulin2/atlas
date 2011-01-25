//package org.atlasapi.remotesite;
//
//import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
//
//import java.util.List;
//
//import org.atlasapi.media.entity.Encoding;
//import org.atlasapi.media.entity.Item;
//import org.atlasapi.media.entity.Location;
//import org.atlasapi.media.entity.Policy;
//import org.atlasapi.media.entity.Publisher;
//import org.atlasapi.media.entity.Version;
//import org.atlasapi.persistence.content.mongo.MongoDbBackedContentStore;
//import org.atlasapi.persistence.logging.AdapterLog;
//import org.atlasapi.persistence.logging.AdapterLogEntry;
//import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
//import org.atlasapi.persistence.media.entity.ItemTranslator;
//import org.joda.time.DateTime;
//import org.joda.time.Interval;
//
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.Iterables;
//import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
//import com.metabroadcast.common.persistence.mongo.MongoConstants;
//import com.metabroadcast.common.time.Clock;
//import com.metabroadcast.common.time.SystemClock;
//import com.mongodb.BasicDBObject;
//import com.mongodb.DBCollection;
//import com.mongodb.DBObject;
//
//public class ItemAvailabilityUpdater implements Runnable {
//
//	private static final String AVAILABLE = "available";
//	private static final String AVAILABILITY_END = "policy.availabilityEnd";
//	private static final String AVAILABILITY_START = "policy.availabilityStart";
//	private static final String AVAILABLE_AT = "versions.manifestedAs.availableAt";
//	private static final String PUBLISHER = "publisher";
//	private static final ImmutableList<String> SELECTED_PUBLISHERS = ImmutableList.of(Publisher.BBC.key(),Publisher.C4.key());
//	
//	private final Clock clock;
//	
//	private final AdapterLog log;
//	private final MongoDbBackedContentStore store;
//
//	public ItemAvailabilityUpdater(MongoDbBackedContentStore store, AdapterLog log, Clock clock) {
//		this.store = store;
//		this.log = log;
//		this.clock = clock;
//	}
//	
//	public ItemAvailabilityUpdater(MongoDbBackedContentStore store, AdapterLog log) {
//		this(store, log, new SystemClock());
//	}
//
//	@Override
//	public void run() {
//		log.record(new AdapterLogEntry(Severity.INFO).withDescription("BBC Availability Updater Running"));
//		try {
//			DateTime now = clock.now();
//			
//			List<DBObject> toUpdate = ImmutableList.copyOf(Iterables.concat(items.find(availableQuery(now)), items.find(notAvailableQuery(now))));
//			for (DBObject dbo : toUpdate) {
//				items.update(new BasicDBObject(MongoConstants.ID, dbo.get(MongoConstants.ID)) , updateAvailabilityOf(dbo, now));
//			}
//		} catch (Exception e) {
//			log.record(new AdapterLogEntry(Severity.WARN).withDescription("BBC Availability Updater didn't complete successfully").withCause(e));
//		}
//		log.record(new AdapterLogEntry(Severity.INFO).withDescription("BBC Availability Updater Completed"));
//	}
//
//	private DBObject availableQuery(DateTime now) {
//		return where().elemMatch(AVAILABLE_AT,
//				where().fieldBefore(AVAILABILITY_START, now).fieldAfter(AVAILABILITY_END, now).fieldEquals(AVAILABLE, false)
//			).fieldIn(PUBLISHER, SELECTED_PUBLISHERS).build();
//	}
//	
//	private DBObject notAvailableQuery(DateTime now) {
//		return where().elemMatch(AVAILABLE_AT,
//				where().or(
//						where().fieldAfter(AVAILABILITY_START, now), 
//						where().fieldBefore(AVAILABILITY_END, now)
//				).fieldEquals(AVAILABLE, true)
//			).fieldIn(PUBLISHER, SELECTED_PUBLISHERS).build();
//	}
//	
//	private Item updateAvailabilityOf(Item item, DateTime now) {
//		for (Version version : item.getVersions()) {
//			for (Encoding encoding : version.getManifestedAs()) {
//				for (Location location : encoding.getAvailableAt()) {
//					Policy policy = location.getPolicy();
//					if (policy != null) {
//						Interval availabilityInterval = new Interval(policy.getAvailabilityStart(), policy.getAvailabilityEnd());
//						location.setAvailable(availabilityInterval.contains(now));
//					}
//				}
//			}
//		}
//		return item;
//	}
//}
