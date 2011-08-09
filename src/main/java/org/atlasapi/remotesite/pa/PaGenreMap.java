package org.atlasapi.remotesite.pa;

import java.util.Set;

import org.atlasapi.genres.AtlasGenre;
import org.atlasapi.genres.GenreMap;

public class PaGenreMap extends GenreMap {
    static {
        genres.put("http://pressassociation.com/genres/2000",            AtlasGenre.NEWS);
        genres.put("http://pressassociation.com/genres/2F02",            AtlasGenre.NEWS);
        genres.put("http://pressassociation.com/genres/2F03",            AtlasGenre.NEWS);
        genres.put("http://pressassociation.com/genres/2F04",            AtlasGenre.NEWS);
        genres.put("http://pressassociation.com/genres/2F05",            AtlasGenre.NEWS);
        genres.put("http://pressassociation.com/genres/2F06",            AtlasGenre.NEWS);
        genres.put("http://pressassociation.com/genres/9000",            AtlasGenre.FACTUAL);
        genres.put("http://pressassociation.com/genres/3100",            AtlasGenre.ENTERTAINMENT);
        genres.put("http://pressassociation.com/genres/1000",            AtlasGenre.FILM);
        genres.put("http://pressassociation.com/genres/5000",            AtlasGenre.CHILDRENS);
    }
}
