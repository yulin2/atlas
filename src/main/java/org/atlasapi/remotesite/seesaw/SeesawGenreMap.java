package org.atlasapi.remotesite.seesaw;

import org.atlasapi.genres.GenreMap;

public class SeesawGenreMap extends GenreMap {
    
    static {
        genres.put("http://www.seesaw.com/TV/Comedy",        "http://ref.atlasapi.org/genres/atlas/comedy");
        genres.put("http://www.seesaw.com/TV/Drama",         "http://ref.atlasapi.org/genres/atlas/drama");
        genres.put("http://www.seesaw.com/TV/Entertainment", "http://ref.atlasapi.org/genres/atlas/entertainment");
        genres.put("http://www.seesaw.com/TV/Factual",       "http://ref.atlasapi.org/genres/atlas/factual");
        genres.put("http://www.seesaw.com/TV/Lifestyle",     "http://ref.atlasapi.org/genres/atlas/lifestyle");
    }
}
