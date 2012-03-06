package org.atlasapi.remotesite.lovefilm;

import org.atlasapi.genres.AtlasGenre;
import org.atlasapi.genres.GenreMap;

public class LoveFilmGenreMap extends GenreMap {
    static {
        genres.put("http://openapi.lovefilm.com/categories/genres/Drama", AtlasGenre.DRAMA);
        genres.put("http://openapi.lovefilm.com/categories/genres/Animated", AtlasGenre.ANIMATION);
        genres.put("http://openapi.lovefilm.com/categories/genres/Comedy", AtlasGenre.COMEDY);
        genres.put("http://openapi.lovefilm.com/categories/genres/Children", AtlasGenre.CHILDRENS);
        genres.put("http://openapi.lovefilm.com/categories/genres/Thriller", AtlasGenre.CHILDRENS);
        genres.put("http://openapi.lovefilm.com/categories/genres/Horror", AtlasGenre.CHILDRENS);
        genres.put("http://openapi.lovefilm.com/categories/genres/Romance", AtlasGenre.CHILDRENS);
        genres.put("http://openapi.lovefilm.com/categories/genres/Sci-Fi/Fantasy", AtlasGenre.CHILDRENS);
        genres.put("http://openapi.lovefilm.com/categories/genres/Action/Adventure", AtlasGenre.CHILDRENS);
        genres.put("http://openapi.lovefilm.com/categories/genres/Sport", AtlasGenre.SPORT);
    }
}
