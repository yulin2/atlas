package org.atlasapi.remotesite.lovefilm;

import static org.atlasapi.remotesite.lovefilm.LoveFilmCsvColumn.ACCESS_METHOD;
import static org.atlasapi.remotesite.lovefilm.LoveFilmCsvColumn.ITEM_TYPE_KEYWORD;

import org.atlasapi.remotesite.lovefilm.LoveFilmData.LoveFilmDataRow;


public class LoveFilmDataRowUtils {
    
    private static final String SHOW = "show";
    private static final String SEASON = "season";
    private static final String EPISODE = "episode";
    private static final String VOD = "VOD";
    private static final String TELE_VIDEO_RECS = "television-video-recordings";
    private static final String MOVIE_VIDEO_RECS = "movie-video-recordings";
    
    public static final String LOVEFILM_URI_PATTERN = "http://lovefilm.com/%s/%s";
    public static final String EPISODE_RESOURCE_TYPE = "episodes";
    public static final String SEASON_RESOURCE_TYPE = "seasons";
    public static final String SHOW_RESOURCE_TYPE = "shows";
    public static final String FILM_RESOURCE_TYPE = "films";
    
    public static boolean isBrand(LoveFilmDataRow row) {
        return LoveFilmCsvColumn.ENTITY.valueIs(row, SHOW);
    }
    
    public static boolean isSeries(LoveFilmDataRow row) {
        return LoveFilmCsvColumn.ENTITY.valueIs(row, SEASON);
    }
    
    public static boolean isEpisode(LoveFilmDataRow row) {
        return LoveFilmCsvColumn.ENTITY.valueIs(row, EPISODE)
            && ACCESS_METHOD.valueIs(row, VOD) 
            && ITEM_TYPE_KEYWORD.valueIs(row, TELE_VIDEO_RECS);
    }
    
    public static boolean isFilm(LoveFilmDataRow row) {
        return ACCESS_METHOD.valueIs(row, VOD)
            && ITEM_TYPE_KEYWORD.valueIs(row, MOVIE_VIDEO_RECS);
    }
}
