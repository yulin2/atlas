package org.atlasapi.remotesite.archiveorg;

import java.util.List;

import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.ContentWriters;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.html.HtmlNavigator;
import org.jdom.Element;

import com.google.common.collect.Lists;
import com.metabroadcast.common.http.SimpleHttpClient;

public class ArchiveOrgPlaylistsUpdater implements Runnable {

    private final static String ARCHIVE_ORG_HOST = "http://www.archive.org";

    private final SimpleHttpClient client;
    private final ArchiveOrgItemAdapter itemAdapter;

    private final Iterable<String> playlistUris;
    private final ContentWriters contentWriter;

    private final AdapterLog log;

    public ArchiveOrgPlaylistsUpdater(SimpleHttpClient client, ArchiveOrgItemAdapter itemAdapter, Iterable<String> playlistUris, ContentWriters contentWriter, AdapterLog log) {
        this.client = client;
        this.itemAdapter = itemAdapter;
        this.playlistUris = playlistUris;
        this.contentWriter = contentWriter;
        this.log = log;
    }

    @Override
    public void run() {
        for (String playlistUri : playlistUris) {
            try {
                boolean noItemsLeft = false;
                int page = 1;
                
                ContentGroup playlist = new ContentGroup();
                playlist.setCanonicalUri(playlistUri);
                playlist.setPublisher(Publisher.ARCHIVE_ORG);
                
                List<Item> items = Lists.newArrayList();
                
                while (!noItemsLeft) {
                    String content = null;
                    String currentPageUri = playlistUri + "&page=" + page++;
                    
                    for (int i = 0; i < 5; i++) {
                        content = client.getContentsOf(currentPageUri);
                        if (content != null) {
                            break;
                        }
                    }
            
                    if (content != null) {
                        HtmlNavigator navigator = new HtmlNavigator(content);
                        
                        if (playlist.getCurie() == null) {
                            Element titleElement = navigator.firstElementOrNull("/html/head/title");
                            String title = titleElement.getValue();
                            
                            String collectionName = title.substring(title.lastIndexOf("collection:") + "collection:".length());
                            
                            playlist.setCurie("arc:" + collectionName);
                        }
                        
                        List<Element> elements = navigator.allElementsMatching("//a[@class='titleLink']");
                        if (!elements.isEmpty()) {
                            for (Element element : elements) {
                                try {
                                    String itemUri = ARCHIVE_ORG_HOST + element.getAttributeValue("href");
                                    if (itemAdapter.canFetch(itemUri)) {
                                        Item item = itemAdapter.fetch(itemUri);
                                        if (item != null) {
                                            contentWriter.createOrUpdate(item);
                                            items.add(item);
                                        }
                                    }
                                }
                                catch (Exception e) {
                                    log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(ArchiveOrgPlaylistsUpdater.class));
                                }
                            }
                        }
                        else {
                            noItemsLeft = true;
                        }
                    } else {
                        throw new FetchException("Playlist page returned with no content: " + currentPageUri);
                    }
                }
                playlist.setContents(items);
                contentWriter.createOrUpdateSkeleton(playlist);
            } catch (Exception e) {
                log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withUri(playlistUri).withSource(ArchiveOrgPlaylistsUpdater.class));
            }
        }
    }
}
