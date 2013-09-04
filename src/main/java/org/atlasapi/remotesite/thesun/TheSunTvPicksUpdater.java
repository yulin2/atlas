package org.atlasapi.remotesite.thesun;

import static org.atlasapi.persistence.logging.AdapterLogEntry.Severity.WARN;
import java.util.Collection;
import java.util.Set;
import nu.xom.Document;
import nu.xom.Nodes;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.system.RemoteSiteClient;

import com.google.common.base.Preconditions;
import com.metabroadcast.common.scheduling.ScheduledTask;


public class TheSunTvPicksUpdater extends ScheduledTask  {
    private final String feedUrl; 
    private final TheSunTvPicksEntryProcessor entryProcessor;
    private final TheSunTvPicksContentGroupUpdater groupUpdater;
    private final RemoteSiteClient<Document> rssFetcher;
    private final AdapterLog log;
    private int itemsProcessed;
    private int groupSize;
    
    public TheSunTvPicksUpdater(String feedUrl, RemoteSiteClient<Document> rssFetcher, TheSunTvPicksEntryProcessor entryProcessor, TheSunTvPicksContentGroupUpdater groupUpdater, AdapterLog log) {
        super();
        this.feedUrl = feedUrl;
        this.rssFetcher = rssFetcher;
        this.entryProcessor = entryProcessor;
        this.groupUpdater = groupUpdater;
        this.log = log;
    }
    
    private Document getFeed(String uri) {
        try {
            return rssFetcher.get(uri);
        } catch (Exception e) {
            log.record(new AdapterLogEntry(WARN).withCause(e).withSource(getClass()).withDescription("Exception fetching feed at " + uri));
            return null;
        }
    }
    
    @Override
    protected void runTask() {
        Document scheduleDocument = getFeed(this.feedUrl);
        Preconditions.checkNotNull(scheduleDocument);
        Nodes entryNodes = scheduleDocument.query("rss/channel/item");
        Collection<Item> items = entryProcessor.convertToItems(entryNodes);
        itemsProcessed = items.size();
        entryProcessor.createOrUpdate(items);
        Set<ChildRef> groupContents = entryProcessor.getChildRefs(items);
        groupSize = groupContents.size();
        // Update content group
        ContentGroup contentGroup = groupUpdater.createOrRetrieveGroup();
        contentGroup.setContents(groupContents);
        groupUpdater.saveGroup(contentGroup);
    }
    
    public int getNumberOfItemsProcessed() {
        return itemsProcessed;
    }
    
    public int getGroupSize() {
        return groupSize;
    }
}