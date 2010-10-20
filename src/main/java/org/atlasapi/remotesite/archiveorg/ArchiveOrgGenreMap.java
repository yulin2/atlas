package org.atlasapi.remotesite.archiveorg;

import org.atlasapi.genres.GenreMap;

public class ArchiveOrgGenreMap extends GenreMap {
    static {
        genres.put("http://www.archive.org/search.php?query=subject:%22comedy%22",          "http://ref.atlasapi.org/genres/atlas/comedy");
        genres.put("http://www.archive.org/search.php?query=subject:%22animals%22",         "http://ref.atlasapi.org/genres/atlas/animals");
                
        genres.put("http://www.archive.org/search.php?query=subject:%22film%22",            "http://ref.atlasapi.org/genres/atlas/film");
        genres.put("http://www.archive.org/search.php?query=subject:%22entertainment%22",   "http://ref.atlasapi.org/genres/atlas/entertainment");
        genres.put("http://www.archive.org/search.php?query=subject:%22childrens%22",       "http://ref.atlasapi.org/genres/atlas/childrens");
        genres.put("http://www.archive.org/search.php?query=subject:%22drama%22",           "http://ref.atlasapi.org/genres/atlas/drama");
        genres.put("http://www.archive.org/search.php?query=subject:%22learning%22",        "http://ref.atlasapi.org/genres/atlas/learning");
        genres.put("http://www.archive.org/search.php?query=subject:%22lifestyle%22",       "http://ref.atlasapi.org/genres/atlas/lifestyle");
        genres.put("http://www.archive.org/search.php?query=subject:%22music%22",           "http://ref.atlasapi.org/genres/atlas/music");
        genres.put("http://www.archive.org/search.php?query=subject:%22news%22",            "http://ref.atlasapi.org/genres/atlas/news");
        genres.put("http://www.archive.org/search.php?query=subject:%22sports%22",          "http://ref.atlasapi.org/genres/atlas/sports");
    }
}
