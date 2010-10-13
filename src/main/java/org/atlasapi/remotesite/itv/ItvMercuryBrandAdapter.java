package org.atlasapi.remotesite.itv;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.SiteSpecificAdapter;

public class ItvMercuryBrandAdapter implements SiteSpecificAdapter<Brand> {
    
    private final static Pattern BRAND_URL = Pattern.compile("https?://www.itv.com/itvplayer/video/?.*Filter=(\\w.+)");
    public static final String BASE_URL = "http://www.itv.com/itvplayer/video/?Filter=";
    private final static String BASE_API = "http://mercury.itv.com/api/json/dotcom/Programme/Index/";
    private final RemoteSiteClient<Map<String, Object>> client;
    private final ContentExtractor<Map<String, Object>, Brand> brandExtractor;
    
    public ItvMercuryBrandAdapter() {
        this(new ItvMercuryClient(), new ItvMercuryBrandExtractor());
    }
    
    public ItvMercuryBrandAdapter(RemoteSiteClient<Map<String, Object>> client, ContentExtractor<Map<String, Object>, Brand> brandExtractor) {
        this.client = client;
        this.brandExtractor = brandExtractor;
    }

    @Override
    public boolean canFetch(String uri) {
        Matcher matcher = BRAND_URL.matcher(uri);
        return matcher.matches();
    }

    @Override
    public Brand fetch(String url) {
        Matcher matcher = BRAND_URL.matcher(url);
        if (matcher.matches()) {
            String pid = matcher.group(1);
            try {
                return brandExtractor.extract(client.get(BASE_API+pid));
            } catch (Exception e) {
                throw new FetchException("Unable to extract brand: "+url, e);
            }
        } else {
            throw new FetchException("Unable to retrieve ITV brand with inappropriate format url: "+url);
        }
    }
}
