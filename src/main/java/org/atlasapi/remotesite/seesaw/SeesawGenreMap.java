package org.atlasapi.remotesite.seesaw;

import org.atlasapi.genres.AtlasGenre;
import org.atlasapi.genres.GenreMap;

public class SeesawGenreMap extends GenreMap {
    
    static {
        genres.put("http://www.seesaw.com/TV/Comedy",        AtlasGenre.COMEDY);
        genres.put("http://www.seesaw.com/TV/Drama",         AtlasGenre.DRAMA);
        genres.put("http://www.seesaw.com/TV/Entertainment", AtlasGenre.ENTERTAINMENT);
        genres.put("http://www.seesaw.com/TV/Factual",       AtlasGenre.FACTUAL);
        genres.put("http://www.seesaw.com/TV/Lifestyle",     AtlasGenre.LIFESTYLE);
    }
}
