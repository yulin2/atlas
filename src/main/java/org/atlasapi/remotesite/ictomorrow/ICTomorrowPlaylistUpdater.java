package org.atlasapi.remotesite.ictomorrow;

import nu.xom.Element;
import nu.xom.Elements;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.remotesite.SiteSpecificAdapter;

import com.metabroadcast.common.social.auth.ictomorrow.ICTomorrowApiException;
import com.metabroadcast.common.social.auth.ictomorrow.ICTomorrowApiHelper;

public class ICTomorrowPlaylistUpdater implements Runnable {
    private final ICTomorrowApiHelper apiHelper;
    private final ContentWriter contentWriter;
    private final Log log = LogFactory.getLog(ICTomorrowPlaylistUpdater.class);
    private final SiteSpecificAdapter<Item> itemAdapter;
    

    public ICTomorrowPlaylistUpdater(ICTomorrowApiHelper apiHelper, ContentWriter contentWriter, SiteSpecificAdapter<Item> itemAdapter) {
        this.apiHelper = apiHelper;
        this.contentWriter = contentWriter;
        this.itemAdapter = itemAdapter;
    }
    
    @Override
    public void run() {
        try {
            Element jobElement = apiHelper.getContentMetadata(null, null, null);
            Integer jobId = Integer.valueOf(jobElement.getChildElements("job_id").get(0).getValue());
            
            Element metadataFile = null;
            while (metadataFile == null) {
                Element returnData = apiHelper.getMetadataFile(jobId);
                
                String returnValue = returnData.getFirstChildElement("job_status").getValue();
                if (returnValue.equals("COMPLETE")) {
                    metadataFile = returnData.getFirstChildElement("Download", "http://www.innovateuk.org/testbed/DownloadContent/").getFirstChildElement("Items");
                }
                else if (returnValue.equals("FAILED")) {
                    throw new ICTomorrowApiException("Metadata File processing failed " + jobId);
                }
                else {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        log.debug("Sleep interrupted while waiting to retry", e);
                    }
                }
            }
            
            Elements itemsElement = metadataFile.getChildElements("Item");
            Playlist ictomorrowPlaylist = new Playlist("http://ictomorrow.co.uk/all-content", "ict:all", Publisher.ICTOMORROW);
            ictomorrowPlaylist.setTitle("Classic Telly");
            ictomorrowPlaylist.setDescription("Classic TV provided by ICTomorrow");
            
            for (int i = 0; i < itemsElement.size(); i++) {
                
                Item item = null;
                Element itemElement = itemsElement.get(i);
                String contentHandle = itemElement.getAttributeValue("ContentHandle");
                String uri = itemElement.getFirstChildElement("Key").getValue();
                if (itemAdapter.canFetch(uri)) {
                    item = itemAdapter.fetch(uri);
                    item.addAlias(getICTomorrowAlias(contentHandle));
                }
                
                if (item != null) {
                    contentWriter.createOrUpdateItem(item);
                    
                    ictomorrowPlaylist.addItem(item);
                }
            }
            
            contentWriter.createOrUpdatePlaylistSkeleton(ictomorrowPlaylist);
        } catch (ICTomorrowApiException e) {
            System.err.println(e.getMessage());
           log.debug("API Exception while updating playlist", e);
        }
    }
    
    private String getICTomorrowAlias(String contentHandle) {
        return "http://ictomorrow.co.uk/contentHandle/" + contentHandle;
    }
}


