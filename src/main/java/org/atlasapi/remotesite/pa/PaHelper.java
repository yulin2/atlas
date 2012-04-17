package org.atlasapi.remotesite.pa;


public class PaHelper {
    
    private static final String PA_BASE_URL = "http://pressassociation.com";
    
    public static String getFilmUri(String id) {
        return PA_BASE_URL + "/films/" + id;
    }
    
    public static String getEpisodeUri(String id) {
        return PA_BASE_URL + "/episodes/" + id;
    }
    
    public static String getBrandUri(String id) {
        return PA_BASE_URL + "/brands/" + id;
    }
    
    public static String getSeriesUri(String id, String seriesNumber) {
        return PA_BASE_URL + "/series/" + id + "-" + seriesNumber;
    }
    
    public static String getFilmCurie(String id) {
        return "pa:f-" + id;
    }
    
    public static String getEpisodeCurie(String id) {
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
