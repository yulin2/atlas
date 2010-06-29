package org.uriplay.remotesite.hulu;

import java.io.StringReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.uriplay.media.entity.Episode;
import org.uriplay.media.entity.Playlist;
import org.uriplay.persistence.system.RemoteSiteClient;
import org.uriplay.remotesite.FetchException;
import org.uriplay.remotesite.HttpClients;
import org.uriplay.remotesite.SiteSpecificAdapter;

import com.metabroadcast.common.http.SimpleHttpClient;
import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Guid;
import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.io.WireFeedInput;

public class HuluRssAdapter implements SiteSpecificAdapter<Playlist> {

    public static final String BASE_URI = "http://www.hulu.com/feed/";
    public static final Pattern URI_PATTERN = Pattern.compile("^(.*watch\\/\\d+)\\/.*$");

    private final SiteSpecificAdapter<Episode> huluItemAdapter;
    private final RemoteSiteClient<Channel> feedClient;

    public HuluRssAdapter() {
        this(rssClient(), new HuluItemAdapter());
    }
    
    public HuluRssAdapter(SiteSpecificAdapter<Episode> huluItemAdapter) {
        this(rssClient(), huluItemAdapter);
    }

    public HuluRssAdapter(RemoteSiteClient<Channel> feedClient, SiteSpecificAdapter<Episode> huluItemAdapter) {
        this.feedClient = feedClient;
        this.huluItemAdapter = huluItemAdapter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Playlist fetch(String uri) {
        try {
            Playlist playlist = new Playlist();

            Channel channel = feedClient.get(uri);
            playlist.setTitle(channel.getTitle());
            playlist.setDescription(channel.getDescription());
            playlist.setCanonicalUri(uri);
            playlist.setCurie("hulu:" + uri.replace(BASE_URI, "").replace("/", "_"));

            List<Item> items = channel.getItems();
            for (Item rssItem : items) {
                Guid guid = rssItem.getGuid();
                if (guid != null) {
                    
                    Matcher matcher = URI_PATTERN.matcher(guid.getValue());
                    if (matcher.matches()) {
                        
                        Episode item = huluItemAdapter.fetch(matcher.group(1));
                        playlist.addItem(item);
                    }
                }
            }

            return playlist;
        } catch (Exception e) {
            throw new FetchException("Unable to retrieve Hulu feed from: " + uri, e);
        }
    }

    @Override
    public boolean canFetch(String uri) {
        return uri.startsWith(BASE_URI);
    }

    private static RemoteSiteClient<Channel> rssClient() {

        return new RemoteSiteClient<Channel>() {

            private final SimpleHttpClient client = HttpClients.webserviceClient();

            @Override
            public Channel get(String uri) throws Exception {
                return (Channel) new WireFeedInput().build(new StringReader(client.getContentsOf(uri)));
            }
        };
    }
}
