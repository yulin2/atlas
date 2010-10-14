package org.atlasapi.remotesite.itv;

import org.atlasapi.genres.GenreMap;

public class ItvGenreMap extends GenreMap {
    static {
        genres.put("entertainment", "http://ref.atlasapi.org/genres/atlas/entertainment");
        genres.put("lifestyle", "http://ref.atlasapi.org/genres/atlas/lifestyle");
        genres.put("drama", "http://ref.atlasapi.org/genres/atlas/drama");
        genres.put("soaps", "http://ref.atlasapi.org/genres/atlas/drama");
        genres.put("sport", "http://ref.atlasapi.org/genres/atlas/sport");
    }
}
