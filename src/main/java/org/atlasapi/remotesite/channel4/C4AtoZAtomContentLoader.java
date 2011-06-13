package org.atlasapi.remotesite.channel4;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.system.AToZUriSource;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.SiteSpecificAdapter;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;

public class C4AtoZAtomContentLoader implements Runnable {
	
    private static final Pattern PAGE_PATTERN = Pattern.compile("(http://api.channel4.com/programmes/atoz/.+/page-\\d+.atom).*");
    
    private final RemoteSiteClient<Feed> feedClient;
    private final C4AtomBackedBrandAdapter brandAdapter;
	private final AdapterLog log;
    
    public C4AtoZAtomContentLoader(RemoteSiteClient<Feed> feedClient, C4AtomBackedBrandAdapter brandExtractor, AdapterLog log) {
        this.feedClient = feedClient;
        this.brandAdapter = brandExtractor;
		this.log = log;
    }

    @VisibleForTesting
    void loadAndSaveByLetter(String letter) throws Exception {
            
            boolean hasNext = false;
            String currentPage = C4AtomApi.createAtoZRequest(letter, ".atom");
            do {
                Feed feed = feedClient.get(currentPage);
                loadAndSaveFromFeed(feed);
                String nextUrl = extractNextLinkFromLinks(feed);
                if (nextUrl != null) {
                    hasNext = true;
                    currentPage = nextUrl;
                } else {
                    hasNext = false;
                }
            } while (hasNext);
    }

	@SuppressWarnings("unchecked")
	private void loadAndSaveFromFeed(Feed feed) {
		for (Entry entry: (List<Entry>) feed.getEntries()) {
		    String brandUri = extarctUriFromLinks(entry);
		    if (brandUri != null && brandAdapter.canFetch(brandUri)) {
		        writeBrand(brandUri);
		    }
		}
	}

	private void writeBrand(String brandUri) {
		try {
			 brandAdapter.writeBrandFrom(brandUri);
		} catch (Exception e) {
			log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withUri(brandUri).withSource(brandAdapter.getClass()));
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
