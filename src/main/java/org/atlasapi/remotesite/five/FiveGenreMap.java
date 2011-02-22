package org.atlasapi.remotesite.five;

import org.atlasapi.genres.AtlasGenre;
import org.atlasapi.genres.GenreMap;

public class FiveGenreMap extends GenreMap {
    static {
        genres.put("http://www.five.tv/genres/drama",          AtlasGenre.DRAMA);
        genres.put("http://www.five.tv/genres/documentary",    AtlasGenre.FACTUAL);
        genres.put("http://www.five.tv/genres/entertainment",  AtlasGenre.ENTERTAINMENT);
        genres.put("http://www.five.tv/genres/sport",          AtlasGenre.SPORT);
        genres.put("http://www.five.tv/genres/milkshake!",     AtlasGenre.CHILDRENS);
    }
}
