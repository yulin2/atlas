package org.atlasapi.remotesite.worldservice;

import org.atlasapi.media.common.Id;
import org.atlasapi.remotesite.worldservice.model.WsProgramme;
import org.atlasapi.remotesite.worldservice.model.WsSeries;

public class WorldServiceIds {
    
    private static final String WS_ARCHIVE_BASE = "http://wsarchive.bbc.co.uk/";
    private static final String WS_ARCHIVE_BRAND_BASE = WS_ARCHIVE_BASE + "brands/";
    private static final String WS_ARCHIVE_EPISODE_BASE = WS_ARCHIVE_BASE + "episodes/";
    private static final String WS_BRAND_CURIE = "ws-b:";
    private static final String WS_EPISODE_CURIE = "ws-e:";
    
    
    public static Id uriForBrand(String id) {
        return null;//WS_ARCHIVE_BRAND_BASE + id;
    }

    public static Id uriFor(WsSeries series) {
        return uriForBrand(series.getSeriesId());
    }
    
    public static String uriFor(WsProgramme programme) {
        return WS_ARCHIVE_EPISODE_BASE + programme.getProgId();
    }
    
    public static String curieFor(WsSeries series) {
        return WS_BRAND_CURIE + series.getSeriesId();
    }
    
    public static String curieFor(WsProgramme programme) {
        return WS_EPISODE_CURIE + programme.getProgId();
    }
    
}
