package org.atlasapi.remotesite.bbc.ion;

import java.util.Set;

import org.atlasapi.remotesite.bbc.BbcProgrammesGenreMap;
import org.atlasapi.remotesite.bbc.ion.model.IonGenre;

import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.ImmutableSet;

public class BbcIonGenreMap {

    private final static String PREFIX = "http://www.bbc.co.uk/programmes/genres/";
    private final BbcProgrammesGenreMap bbcProgrammesGenreMap;
    
    public BbcIonGenreMap(BbcProgrammesGenreMap bbcProgrammesGenreMap) {
        this.bbcProgrammesGenreMap = bbcProgrammesGenreMap;
    }
    
    public Set<String> fromIon(Iterable<IonGenre> ionGenres) {
        if(ionGenres == null) {
            return ImmutableSet.of();
        }
        Builder<String> genres = ImmutableSet.builder();
        for(IonGenre genre : ionGenres) {
            if(genre.getLevel() < 3) {
                String g = PREFIX + genre.getPath().replace("_", "");
                genres.add(g);
            }
        }
        return bbcProgrammesGenreMap.map(genres.build());
    }
}
