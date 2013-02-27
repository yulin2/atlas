/* Copyright 2009 British Broadcasting Corporation
   Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.remotesite.youtube;

import org.atlasapi.genres.AtlasGenre;
import org.atlasapi.genres.GenreMap;

/**
 * Mapping from YouTube's genre uris to URIplay genre uris.
 *  
 * @author Chris Jackson (chris@metabroadcast.com)
 * @author Robert Chatley (robert@metabroadcast.com)
 * @deprecated
 */
class YouTubeGenreMap extends GenreMap {
//TODO is this used at all, other than the tests?
    static {
        genres.put("http://ref.atlasapi.org/genres/youtube/autos",         AtlasGenre.LIFESTYLE);
        genres.put("http://ref.atlasapi.org/genres/youtube/comedy",        AtlasGenre.COMEDY);
        genres.put("http://ref.atlasapi.org/genres/youtube/education",     AtlasGenre.LEARNING);
        genres.put("http://ref.atlasapi.org/genres/youtube/entertainment", AtlasGenre.ENTERTAINMENT);
        genres.put("http://ref.atlasapi.org/genres/youtube/film",          AtlasGenre.FILM);
        genres.put("http://ref.atlasapi.org/genres/youtube/games",         AtlasGenre.ENTERTAINMENT);
        genres.put("http://ref.atlasapi.org/genres/youtube/howto",         AtlasGenre.LIFESTYLE);
        genres.put("http://ref.atlasapi.org/genres/youtube/movies",        AtlasGenre.FILM);
        genres.put("http://ref.atlasapi.org/genres/youtube/music",         AtlasGenre.MUSIC);
        genres.put("http://ref.atlasapi.org/genres/youtube/news",          AtlasGenre.NEWS);
        genres.put("http://ref.atlasapi.org/genres/youtube/nonprofit",     AtlasGenre.FACTUAL);
        genres.put("http://ref.atlasapi.org/genres/youtube/people",        AtlasGenre.ENTERTAINMENT);
        genres.put("http://ref.atlasapi.org/genres/youtube/animals",       AtlasGenre.ANIMALS);
        genres.put("http://ref.atlasapi.org/genres/youtube/tech",          AtlasGenre.FACTUAL);
        genres.put("http://ref.atlasapi.org/genres/youtube/sports",        AtlasGenre.SPORT);
        genres.put("http://ref.atlasapi.org/genres/youtube/travel",        AtlasGenre.FACTUAL);
     }
}
