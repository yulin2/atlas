package org.atlasapi.remotesite.archiveorg;

import org.atlasapi.genres.AtlasGenre;
import org.atlasapi.genres.GenreMap;

public class ArchiveOrgGenreMap extends GenreMap {
    static {
        genres.put("http://www.archive.org/search.php?query=subject:%22comedy%22",          AtlasGenre.COMEDY);
        genres.put("http://www.archive.org/search.php?query=subject:%22animals%22",         AtlasGenre.ANIMALS);
        genres.put("http://www.archive.org/search.php?query=subject:%22animation%22",       AtlasGenre.ANIMATION);
        genres.put("http://www.archive.org/search.php?query=subject:%22film%22",            AtlasGenre.FILM);
        genres.put("http://www.archive.org/search.php?query=subject:%22entertainment%22",   AtlasGenre.ENTERTAINMENT);
        genres.put("http://www.archive.org/search.php?query=subject:%22childrens%22",       AtlasGenre.CHILDRENS);
        genres.put("http://www.archive.org/search.php?query=subject:%22drama%22",           AtlasGenre.DRAMA);
        genres.put("http://www.archive.org/search.php?query=subject:%22learning%22",        AtlasGenre.LEARNING);
        genres.put("http://www.archive.org/search.php?query=subject:%22lifestyle%22",       AtlasGenre.LIFESTYLE);
        genres.put("http://www.archive.org/search.php?query=subject:%22music%22",           AtlasGenre.MUSIC);
        genres.put("http://www.archive.org/search.php?query=subject:%22news%22",            AtlasGenre.NEWS);
        genres.put("http://www.archive.org/search.php?query=subject:%22sports%22",          AtlasGenre.SPORT);
    }
}
