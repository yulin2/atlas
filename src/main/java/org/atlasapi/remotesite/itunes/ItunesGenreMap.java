package org.atlasapi.remotesite.itunes;

import org.atlasapi.genres.AtlasGenre;
import org.atlasapi.genres.GenreMap;

public class ItunesGenreMap extends GenreMap {
    static {
        genres.put("http://itunes.apple.com/WebObjects/MZStore.woa/wa/viewGenre?id=4000", AtlasGenre.COMEDY);
        genres.put("http://itunes.apple.com/WebObjects/MZStore.woa/wa/viewGenre?id=4001", AtlasGenre.DRAMA);
        genres.put("http://itunes.apple.com/WebObjects/MZStore.woa/wa/viewGenre?id=4002", AtlasGenre.ANIMATION);
        //genres.put("http://itunes.apple.com/WebObjects/MZStore.woa/wa/viewGenre?id=4003", AtlasGenre.ACTIONADVENTURE);
        //genres.put("http://itunes.apple.com/WebObjects/MZStore.woa/wa/viewGenre?id=4004", AtlasGenre.CLASSIC);
        genres.put("http://itunes.apple.com/WebObjects/MZStore.woa/wa/viewGenre?id=4005", AtlasGenre.CHILDRENS);
        genres.put("http://itunes.apple.com/WebObjects/MZStore.woa/wa/viewGenre?id=4006", AtlasGenre.FACTUAL);
        //genres.put("http://itunes.apple.com/WebObjects/MZStore.woa/wa/viewGenre?id=4007", AtlasGenre.REALITY);
        //genres.put("http://itunes.apple.com/WebObjects/MZStore.woa/wa/viewGenre?id=4008", AtlasGenre.SCIFI);
        genres.put("http://itunes.apple.com/WebObjects/MZStore.woa/wa/viewGenre?id=4009", AtlasGenre.SPORT);
        //genres.put("http://itunes.apple.com/WebObjects/MZStore.woa/wa/viewGenre?id=4010", AtlasGenre.TEENS);
        //genres.put("http://itunes.apple.com/WebObjects/MZStore.woa/wa/viewGenre?id=4011", AtlasGenre.LATINOTV);
    }
}
