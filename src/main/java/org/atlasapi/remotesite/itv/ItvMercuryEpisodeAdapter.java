package org.atlasapi.remotesite.itv;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.content.Episode;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.SiteSpecificAdapter;

public class ItvMercuryEpisodeAdapter implements SiteSpecificAdapter<Episode> {
    
    private final static Pattern EPISODE_URL = Pattern.compile("https?://www.itv.com/itvplayer/video/?.*Filter=(\\d+)");
    public final static String BASE_API = "http://mercury.itv.com/api/json/dotcom/Episode/Index/";
    private final RemoteSiteClient<Map<String, Object>> client;
    private final ContentExtractor<Map<String, Object>, Episode> episodeExtractor;
    
    public ItvMercuryEpisodeAdapter() {
        this(new ItvMercuryClient(), new ItvMercuryEpisodeExtractor());
    }
    
    public ItvMercuryEpisodeAdapter(RemoteSiteClient<Map<String, Object>> client, ContentExtractor<Map<String, Object>, Episode> episodeExtractor) {
        this.client = client;
        this.episodeExtractor = episodeExtractor;
    }

    @Override
    public boolean canFetch(String uri) {
        Matcher matcher = EPISODE_URL.matcher(uri);
        return matcher.matches();
    }

    @Override
    public Episode fetch(String url) {
        Matcher matcher = EPISODE_URL.matcher(url);
        if (matcher.matches()) {
            String pid = matcher.group(1);
            try {
                return episodeExtractor.extract(client.get(BASE_API+pid));
            } catch (Exception e) {
                throw new FetchException("Unable to extract brand: "+url, e);
            }
        } else {
            throw new FetchException("Unable to retrieve ITV brand with inappropriate format url: "+url);
        }
    }
}
