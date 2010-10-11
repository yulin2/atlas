package org.atlasapi.remotesite.ictomorrow;

import nu.xom.Element;
import nu.xom.Elements;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentWriter;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.social.auth.ictomorrow.ICTomorrowApiException;
import com.metabroadcast.common.social.auth.ictomorrow.ICTomorrowApiHelper;

public class ICTomorrowPlaylistUpdater implements Runnable {
    private static final String ARCHIVE_ORG_EMBED_TEMPLATE = "<object width=\"640\" height=\"506\" classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\"><param value=\"true\" name=\"allowfullscreen\"/><param value=\"always\" name=\"allowscriptaccess\"/><param value=\"high\" name=\"quality\"/><param value=\"true\" name=\"cachebusting\"/><param value=\"#000000\" name=\"bgcolor\"/><param name=\"movie\" value=\"http://www.archive.org/flow/flowplayer.commercial-3.2.1.swf\" /><param value=\"config={'key':'#$aa4baff94a9bdcafce8','playlist':['format=Thumbnail?.jpg',{'autoPlay':false,'url':'%1$s_512kb.mp4'}],'clip':{'autoPlay':true,'baseUrl':'http://www.archive.org/download/%1$s/','scaling':'fit','provider':'h264streaming'},'canvas':{'backgroundColor':'#000000','backgroundGradient':'none'},'plugins':{'controls':{'playlist':false,'fullscreen':true,'height':26,'backgroundColor':'#000000','autoHide':{'fullscreenOnly':true}},'h264streaming':{'url':'http://www.archive.org/flow/flowplayer.pseudostreaming-3.2.1.swf'}},'contextMenu':[{},'-','Flowplayer v3.2.1']}\" name=\"flashvars\"/><embed src=\"http://www.archive.org/flow/flowplayer.commercial-3.2.1.swf\" type=\"application/x-shockwave-flash\" width=\"640\" height=\"506\" allowfullscreen=\"true\" allowscriptaccess=\"always\" cachebusting=\"true\" bgcolor=\"#000000\" quality=\"high\" flashvars=\"config={'key':'#$aa4baff94a9bdcafce8','playlist':['format=Thumbnail?.jpg',{'autoPlay':false,'url':'%1$s_512kb.mp4'}],'clip':{'autoPlay':true,'baseUrl':'http://www.archive.org/download/%1$s/','scaling':'fit','provider':'h264streaming'},'canvas':{'backgroundColor':'#000000','backgroundGradient':'none'},'plugins':{'controls':{'playlist':false,'fullscreen':true,'height':26,'backgroundColor':'#000000','autoHide':{'fullscreenOnly':true}},'h264streaming':{'url':'http://www.archive.org/flow/flowplayer.pseudostreaming-3.2.1.swf'}},'contextMenu':[{},'-','Flowplayer v3.2.1']}\"> </embed></object>";
    private static final String ARCHIVE_ORG_DOWNLOAD_TEMPLATE = "http://www.archive.org/download/%1$s/%1$s_512kb.mp4";
    private static final String ARCHIVE_ORG_THUMBNAIL_TEMPLATE = "http://www.archive.org/download/%1$s/%1$s.thumbs/%1$s_000030.jpg";
    
    private final ICTomorrowApiHelper apiHelper;
    private final ContentWriter contentWriter;
    private final Log log = LogFactory.getLog(ICTomorrowPlaylistUpdater.class);
    

    public ICTomorrowPlaylistUpdater(ICTomorrowApiHelper apiHelper, ContentWriter contentWriter) {
        this.apiHelper = apiHelper;
        this.contentWriter = contentWriter;
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
                String uri = itemElement.getFirstChildElement("Key").getValue();
                if (uri.startsWith("http://www.archive.org/")) {
                    String itemName = uri.substring(uri.lastIndexOf("/") + 1);
                    item = new Item(uri, "ict:" + itemName.toLowerCase(), Publisher.ICTOMORROW);
                    Version version = new Version();
                    Encoding encoding = new Encoding();
                    
                    /*Location downloadLocation = new Location();
                    downloadLocation.setTransportType(TransportType.DOWNLOAD);
                    downloadLocation.setAvailable(true);
                    downloadLocation.setUri(String.format(ARCHIVE_ORG_DOWNLOAD_TEMPLATE, itemName));
                    encoding.addAvailableAt(downloadLocation);*/
                    
                    /*Location embedLocation = new Location();
                    embedLocation.setTransportType(TransportType.EMBED);
                    embedLocation.setAvailable(true);
                    embedLocation.setEmbedCode(String.format(ARCHIVE_ORG_EMBED_TEMPLATE, itemName));
                    encoding.addAvailableAt(embedLocation);*/
                    
                    Location linkLocation = new Location();
                    linkLocation.setTransportType(TransportType.LINK);
                    linkLocation.setAvailable(true);
                    linkLocation.setUri(uri);
                    encoding.addAvailableAt(linkLocation);
                    
                    version.addManifestedAs(encoding);
                    item.setVersions(ImmutableSet.of(version));
                    
                    Element titleElement = itemElement.getFirstChildElement("Title");
                    if (titleElement != null) {
                        item.setTitle(titleElement.getValue());
                    }
                    item.setThumbnail(String.format(ARCHIVE_ORG_THUMBNAIL_TEMPLATE, itemName));
                    item.setImage(String.format(ARCHIVE_ORG_THUMBNAIL_TEMPLATE, itemName));
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
}


