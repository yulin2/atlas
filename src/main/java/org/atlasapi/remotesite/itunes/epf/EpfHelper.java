package org.atlasapi.remotesite.itunes.epf;

public class EpfHelper {

    public static final String itunesUriBase = "http://itunes.apple.com/";
    public static final String itunesCurieBase = "itunes";
    
    private static String uriPattern = itunesUriBase + "%s/id%s";
    
    private static String uriForIdType(String type, Integer id) {
        return String.format(uriPattern, type, id);
    }

    private static String curieForIdType(String infix, Integer id) {
        return String.format("%s:%s-%s", itunesCurieBase, infix, id);
    }
    
    public static String uriForBrand(Integer id) {
        return uriForIdType("artist", id);
    }
    
    public static String curieForBrand(Integer id) {
        return curieForIdType("a",id);
    }

    public static String uriForSeries(Integer id) {
        return uriForIdType("tv-season", id);
    }
    
    public static String curieForSeries(Integer id) {
        return curieForIdType("t",id);
    }
    
    public static String uriForEpisode(Integer id) {
        return uriForIdType("video", id);
    }
    
    public static String curieForEpisode(Integer id) {
        return curieForIdType("v", id);
    }
}
