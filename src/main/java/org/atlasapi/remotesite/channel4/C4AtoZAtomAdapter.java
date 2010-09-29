package org.atlasapi.remotesite.channel4;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.query.content.PerPublisherCurieExpander;
import org.atlasapi.remotesite.SiteSpecificAdapter;

import com.metabroadcast.common.http.HttpStatusCodeException;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;

public class C4AtoZAtomAdapter implements SiteSpecificAdapter<Playlist> {
    private static final Pattern C4_ATOZ_URI_PATTERN = Pattern.compile("http://www.channel4.com/programmes/atoz/([a-z]|0-9)");
    private static final Pattern PAGE_PATTERN = Pattern.compile("(http://api.channel4.com/programmes/atoz/.+/page-\\d+.atom).*");
    private final RemoteSiteClient<Feed> feedClient;
    private final SiteSpecificAdapter<Brand> brandAdapter;
    
    public C4AtoZAtomAdapter(RemoteSiteClient<Feed> feedClient, SiteSpecificAdapter<Brand> brandAdapter) {
        this.feedClient = feedClient;
        this.brandAdapter = brandAdapter;
    }

    private String atomUrl(String url) {
        Matcher matcher = C4_ATOZ_URI_PATTERN.matcher(url);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Cannot fetch " + url + " because it is not a valid C4 atoz uri");
        }
        String letter = matcher.group(1);
        return C4AtomApi.createAtoZRequest(letter, ".atom");
    }

    @Override
    public boolean canFetch(String uri) {
        return C4_ATOZ_URI_PATTERN.matcher(uri).matches();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Playlist fetch(String uri) {
        try {
            Playlist playlist = new Playlist(uri, PerPublisherCurieExpander.CurieAlgorithm.C4.compact(uri), Publisher.C4);
            
            Boolean hasNext = false;
            String currentPage = atomUrl(uri);
            do {
                Feed feed = feedClient.get(currentPage);
            
                for (Entry entry: (List<Entry>) feed.getEntries()) {
                    String brandUri = extarctUriFromLinks(entry);
                    if (brandUri != null && brandAdapter.canFetch(brandUri)) {
                        Brand brand = brandAdapter.fetch(brandUri);
                        playlist.addPlaylist(brand);
                    }
                }
                
                String nextUrl = extractNextLinkFromLinks(feed);
                if (nextUrl != null) {
                    hasNext = true;
                    currentPage = nextUrl;
                } else {
                    hasNext = false;
                }
            } while (hasNext);
            
            return playlist;
        } catch (HttpStatusCodeException e) {
            if (HttpServletResponse.SC_NOT_FOUND == e.getStatusCode()) {
                // Return null to signify atoz not found on C4
                return null;
            }
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
}
