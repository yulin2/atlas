package org.atlasapi.remotesite.itv;

import org.atlasapi.genres.AtlasGenre;
import org.atlasapi.genres.GenreMap;

public class ItvGenreMap extends GenreMap {
    static {
        genres.put("entertainment", AtlasGenre.ENTERTAINMENT);
        genres.put("lifestyle", AtlasGenre.LIFESTYLE);
        genres.put("drama", AtlasGenre.DRAMA);
        genres.put("soaps", AtlasGenre.DRAMA);
        genres.put("sport", AtlasGenre.SPORT);
    }
}
