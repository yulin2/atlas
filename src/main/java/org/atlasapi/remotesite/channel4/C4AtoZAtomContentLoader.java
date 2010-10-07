package org.atlasapi.remotesite.channel4;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.system.AToZUriSource;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.query.content.PerPublisherCurieExpander;
import org.atlasapi.remotesite.SiteSpecificAdapter;

import com.google.common.annotations.VisibleForTesting;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;

public class C4AtoZAtomContentLoader implements Runnable {
	
    private static final String C4_ATOZ_URI_FORMAT=  "http://www.channel4.com/programmes/atoz/%s";
    private static final Pattern PAGE_PATTERN = Pattern.compile("(http://api.channel4.com/programmes/atoz/.+/page-\\d+.atom).*");
    
    private final RemoteSiteClient<Feed> feedClient;
    private final SiteSpecificAdapter<Brand> brandAdapter;
	private final AdapterLog log;
	private final ContentWriter writer;
    
    public C4AtoZAtomContentLoader(RemoteSiteClient<Feed> feedClient, SiteSpecificAdapter<Brand> brandAdapter, ContentWriter writer, AdapterLog log) {
        this.feedClient = feedClient;
        this.brandAdapter = brandAdapter;
		this.writer = writer;
		this.log = log;
    }

    @VisibleForTesting
    void loadAndSaveByLetter(String letter) throws Exception {
        	String playlistUri = String.format(C4_ATOZ_URI_FORMAT, letter);
        	
            Playlist playlist = new Playlist(playlistUri, PerPublisherCurieExpander.CurieAlgorithm.C4.compact(playlistUri), Publisher.C4);
            
            boolean hasNext = false;
            String currentPage = C4AtomApi.createAtoZRequest(letter, ".atom");
            do {
                Feed feed = feedClient.get(currentPage);
                loadFromFeed(playlist, feed);
                
                String nextUrl = extractNextLinkFromLinks(feed);
                if (nextUrl != null) {
                    hasNext = true;
                    currentPage = nextUrl;
                } else {
                    hasNext = false;
                }
            } while (hasNext);
            
        	writer.createOrUpdatePlaylist(playlist, true);
    }

	@SuppressWarnings("unchecked")
	private void loadFromFeed(Playlist playlist, Feed feed) {
		for (Entry entry: (List<Entry>) feed.getEntries()) {
		    String brandUri = extarctUriFromLinks(entry);
		    if (brandUri != null && brandAdapter.canFetch(brandUri)) {
		        Brand brand = brandAdapter.fetch(brandUri);
		        if (brand != null) {
		        	playlist.addPlaylist(brand);
		        	writer.createOrUpdatePlaylist(brand, true);
		        }
		    }
		}
	}
    
    @SuppressWarnings("unchecked")
    private String extarctUriFromLinks(Entry entry) {
        for (Link link : (List<Link>) entry.getAlternateLinks()) {
            if ("alternate".equals(link.getRel())) {
                return link.getHref();
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private String extractNextLinkFromLinks(Feed feed) {
        for (Link link : (List<Link>) feed.getOtherLinks()) {
            if ("next".equals(link.getRel())) {
                String next = link.getHref();
                Matcher matcher = PAGE_PATTERN.matcher(next);
                if (matcher.matches()) {
                    return matcher.group(1);
                }
            }
        }
        return null;
    }

	@Override
	public void run() {
		loadAtoZ();
	}

	private void loadAtoZ() {
		for (String letter : new AToZUriSource("", "", true)) {
			try {
				loadAndSaveByLetter(letter);
			} catch (Exception e) {
				log.record(new AdapterLogEntry(Severity.INFO).withCause(e).withDescription("Failed to load C4 atoz playlist for letter: " + letter).withSource(getClass()));
			}
		}
	}
}
