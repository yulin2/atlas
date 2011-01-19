package org.atlasapi.remotesite.ictomorrow;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.ContentWriters;
import org.atlasapi.remotesite.SiteSpecificAdapter;

import com.google.common.collect.Lists;
import com.metabroadcast.common.social.auth.ictomorrow.ICTomorrowApiException;
import com.metabroadcast.common.social.auth.ictomorrow.ICTomorrowApiHelper;
import com.metabroadcast.common.social.auth.ictomorrow.ICTomorrowItemMetadata;

public class ICTomorrowPlaylistUpdater implements Runnable {
    private final ICTomorrowApiHelper apiHelper;
    private final ContentWriters contentWriter;
    private final Log log = LogFactory.getLog(ICTomorrowPlaylistUpdater.class);
    private final SiteSpecificAdapter<Item> itemAdapter;
    private final Integer csaId;
    

    public ICTomorrowPlaylistUpdater(ICTomorrowApiHelper apiHelper, ContentWriters contentWriter, SiteSpecificAdapter<Item> itemAdapter, Integer csaId) {
        this.apiHelper = apiHelper;
        this.contentWriter = contentWriter;
        this.itemAdapter = itemAdapter;
        this.csaId = csaId;
    }
    
    @Override
    public void run() {
        try {
            log.info("Requesting content metadata with csa_id " + csaId);
            int jobId = apiHelper.getContentMetadata(csaId, null, null);
            log.info("Request successful, metadata job number " + jobId);
            
            List<ICTomorrowItemMetadata> items = null;
            while (items == null) {
                log.info("Requesting metadata file for job id " + jobId);
                items = apiHelper.getMetadataFile(jobId);
                
                if (items == null) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        log.debug("Sleep interrupted while waiting to retry", e);
                    }
                }
            }
            
            ContentGroup ictomorrowPlaylist = new ContentGroup("http://ictomorrow.co.uk/all-content", "ict:all", Publisher.ICTOMORROW);
            log.info("Metadata file recieved for job " + jobId);
            
            ictomorrowPlaylist.setTitle("Classic Telly");
            ictomorrowPlaylist.setDescription("Classic TV provided by ICTomorrow");
            
            List<Item> parsedItems = Lists.newArrayList();
            
            for (ICTomorrowItemMetadata itemMetadata : items) {
                Item item = null;
                
                if (itemAdapter.canFetch(itemMetadata.getLink())) {
                    item = itemAdapter.fetch(itemMetadata.getLink());
                    item.addAlias(getICTomorrowAlias(itemMetadata.getContentHandle()));
                }
                
                if (item != null) {
                    contentWriter.createOrUpdate(item);
                    parsedItems.add(item);
                }
            }
            ictomorrowPlaylist.setContents(parsedItems);
            contentWriter.createOrUpdateSkeleton(ictomorrowPlaylist);
        } catch (ICTomorrowApiException e) {
            log.debug("API Exception while updating playlist", e);
        }
    }
    
    private String getICTomorrowAlias(Integer contentHandle) {
        return "http://ictomorrow.co.uk/contentHandle/" + contentHandle;
    }
}


