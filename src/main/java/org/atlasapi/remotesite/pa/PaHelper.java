package org.atlasapi.remotesite.pa;

import org.atlasapi.media.entity.Alias;


public class PaHelper {
    
    private static final String PA_BASE_URL = "http://pressassociation.com";
    private static final String PA_BASE_ALIAS = "pa:";
    
    public static String getFilmUri(String id) {
        return getEpisodeUri(id);
    }
    
    public static String getFilmRtAlias(String rtFilmNumber) {
        return PA_BASE_URL + "/films/" + rtFilmNumber;
    }
    
    public static Alias getFilmAlias(String id) {
        return new Alias(PA_BASE_ALIAS + "film", id);
    }
    
    public static String getEpisodeUri(String id) {
        return PA_BASE_URL + "/episodes/" + id;
    }
    
    public static Alias getEpisodeAlias(String id) {
        return new Alias(PA_BASE_ALIAS + "episode", id);
    }
    
    public static String getBrandUri(String id) {
        return PA_BASE_URL + "/brands/" + id;
    }
    
    public static Alias getBrandAlias(String id) {
        return new Alias(PA_BASE_ALIAS + "brand", id);
    }
    
    public static String getSeriesUri(String id, String seriesNumber) {
        return PA_BASE_URL + "/series/" + id + "-" + seriesNumber;
    }
    
    public static String getEpisodeCurie(String id) {
        return "pa:e-" + id;
    }
    
    public static Alias getSeriesAlias(String id, String seriesNumber) {
        return new Alias(PA_BASE_ALIAS + "series", id + "-" + seriesNumber);
    }
    
    public static String getFilmCurie(String id) {
        return "pa:e-" + id;
    }
    
    public static String getBroadcastId(String slotId) {
        return "pa:" + slotId;
    }

    public static String getBrandCurie(String id) {
        return "pa:b-" + id;
    }
    
    public static String getSeriesCurie(String seriesId, String seriesNumber) {
        return "pa:s-" + seriesId + "-" + seriesNumber;
    }
}
