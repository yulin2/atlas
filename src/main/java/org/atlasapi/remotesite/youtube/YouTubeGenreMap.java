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

import org.atlasapi.genres.GenreMap;

/**
 * Mapping from YouTube's genre uris to URIplay genre uris.
 *  
 * @author Chris Jackson (chris@metabroadcast.com)
 * @author Robert Chatley (robert@metabroadcast.com)
 */
class YouTubeGenreMap extends GenreMap {

    static {
        genres.put("http://ref.atlasapi.org/genres/youtube/autos",         "http://ref.atlasapi.org/genres/atlas/lifestyle");
        genres.put("http://ref.atlasapi.org/genres/youtube/comedy",        "http://ref.atlasapi.org/genres/atlas/comedy");
        genres.put("http://ref.atlasapi.org/genres/youtube/education",     "http://ref.atlasapi.org/genres/atlas/learning");
        genres.put("http://ref.atlasapi.org/genres/youtube/entertainment", "http://ref.atlasapi.org/genres/atlas/entertainment");
        genres.put("http://ref.atlasapi.org/genres/youtube/film",          "http://ref.atlasapi.org/genres/atlas/film");
        genres.put("http://ref.atlasapi.org/genres/youtube/games",         "http://ref.atlasapi.org/genres/atlas/entertainment");
        genres.put("http://ref.atlasapi.org/genres/youtube/howto",         "http://ref.atlasapi.org/genres/atlas/lifestyle");
        genres.put("http://ref.atlasapi.org/genres/youtube/movies",        "http://ref.atlasapi.org/genres/atlas/film");
        genres.put("http://ref.atlasapi.org/genres/youtube/music",         "http://ref.atlasapi.org/genres/atlas/music");
        genres.put("http://ref.atlasapi.org/genres/youtube/news",          "http://ref.atlasapi.org/genres/atlas/news");
        genres.put("http://ref.atlasapi.org/genres/youtube/nonprofit",     "http://ref.atlasapi.org/genres/atlas/factual");
        genres.put("http://ref.atlasapi.org/genres/youtube/people",        "http://ref.atlasapi.org/genres/atlas/entertainment");
        genres.put("http://ref.atlasapi.org/genres/youtube/animals",       "http://ref.atlasapi.org/genres/atlas/animals");
        genres.put("http://ref.atlasapi.org/genres/youtube/tech",          "http://ref.atlasapi.org/genres/atlas/factual");
        genres.put("http://ref.atlasapi.org/genres/youtube/sports",        "http://ref.atlasapi.org/genres/atlas/sports");
        genres.put("http://ref.atlasapi.org/genres/youtube/travel",        "http://ref.atlasapi.org/genres/atlas/factual");
     }
}
