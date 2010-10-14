package org.atlasapi.remotesite.itv;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.SiteSpecificAdapter;

public class ItvMercuryBrandAdapter implements SiteSpecificAdapter<Brand> {

    public final static Pattern BRAND_URL = Pattern.compile("http://www.itv.com/itvplayer/video.*Filter=(.+)");
    public static final String BASE_URL = "http://www.itv.com/itvplayer/video/?Filter=";
    public final static String BASE_API = "http://mercury.itv.com/api/json/dotcom/Programme/Index/";
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
        if (matcher.matches()) {
            String id = matcher.group(1);
            try {
                return ! (Integer.valueOf(id.substring(0, 1)) != null);
            } catch (NumberFormatException e) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Brand fetch(String url) {
        Matcher matcher = BRAND_URL.matcher(url);
        if (matcher.matches()) {
            String pid = matcher.group(1);

            if (!isInt(pid)) {
                pid = pid.replace("...", "").replaceAll(":", "").replace("Celebrity%20Get", "Get").replace("Jack%20Osbourne%20Celebrity", "Celebrity");

                try {
                    pid = extractPid(client.get(ItvMercuryEpisodeAdapter.BASE_API + pid));
                } catch (Exception e) {
                    throw new FetchException("Unable to extract correct brand api call for: " + url, e);
                }
            }

            if (pid != null && Integer.valueOf(pid) > 0) {
                try {
                    return brandExtractor.extract(client.get(BASE_API + pid));
                } catch (Exception e) {
                    throw new FetchException("Unable to extract brand: " + url, e);
                }
            } else {
                throw new FetchException("Unable to extract correct brand api call for: " + url);
            }
        } else {
            throw new FetchException("Unable to retrieve ITV brand with inappropriate format url: " + url);
        }
    }

    private boolean isInt(String id) {
        try {
            return Integer.valueOf(id) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractPid(Map<String, Object> source) {
        if (source.containsKey("Result")) {
            List<Map<String, Object>> results = (List<Map<String, Object>>) source.get("Result");
            for (Map<String, Object> result : results) {
                if (result.containsKey("Details")) {
                    List<Map<String, Object>> programmes = (List<Map<String, Object>>) result.get("Details");
                    for (Map<String, Object> programme : programmes) {
                        if (programme.containsKey("Programme")) {
                            Map<String, Object> programmeInfo = (Map<String, Object>) programme.get("Programme");
                            if (programmeInfo.containsKey("Programme")) {
                                Map<String, String> programmeDetails = (Map<String, String>) programmeInfo.get("Programme");
                                return programmeDetails.get("Id");
                            }
                        }
                    }
                }
            }
        }

        return null;
    }
}
