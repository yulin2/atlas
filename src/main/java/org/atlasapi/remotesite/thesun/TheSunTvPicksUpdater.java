package org.atlasapi.remotesite.thesun;

import java.util.Collection;
import java.util.Set;

import nu.xom.Document;
import nu.xom.Nodes;

import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.metabroadcast.common.scheduling.ScheduledTask;


public class TheSunTvPicksUpdater extends ScheduledTask  {
    private final String feedUrl; 
    private final String contentGroupUri;
    private final TheSunTvPicksEntryProcessor entryProcessor;
    private final TheSunTvPicksContentGroupUpdater groupUpdater;
    private final RemoteSiteClient<Document> rssFetcher;
    private final Logger log = LoggerFactory.getLogger(TheSunTvPicksUpdater.class);
    private int itemsProcessed;
    private int groupSize;
    
    public TheSunTvPicksUpdater(String feedUrl, String contentGroupUri, RemoteSiteClient<Document> rssFetcher, TheSunTvPicksEntryProcessor entryProcessor, TheSunTvPicksContentGroupUpdater groupUpdater) {
        super();
        this.feedUrl = feedUrl;
        this.contentGroupUri = contentGroupUri;
        this.rssFetcher = rssFetcher;
        this.entryProcessor = entryProcessor;
        this.groupUpdater = groupUpdater;
    }
    
    private Document getFeed(String uri) {
        try {
            return rssFetcher.get(uri);
        } catch (Exception e) {
            log.warn("Exception fetching feed at " + uri);
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
        groupUpdater.updateGroup(contentGroupUri, groupContents);
    }
    
    public int getNumberOfItemsProcessed() {
        return itemsProcessed;
    }
    
    public int getGroupSize() {
        return groupSize;
    }
}
