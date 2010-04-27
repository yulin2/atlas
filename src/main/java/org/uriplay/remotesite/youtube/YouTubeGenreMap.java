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

package org.uriplay.remotesite.youtube;

import org.uriplay.genres.GenreMap;

/**
 * Mapping from YouTube's genre uris to URIplay genre uris.
 *  
 * @author Chris Jackson (chris@metabroadcast.com)
 * @author Robert Chatley (robert@metabroadcast.com)
 */
class YouTubeGenreMap extends GenreMap {

    static {
        genres.put("http://uriplay.org/genres/youtube/autos",         "http://uriplay.org/genres/uriplay/lifestyle");
        genres.put("http://uriplay.org/genres/youtube/comedy",        "http://uriplay.org/genres/uriplay/comedy");
        genres.put("http://uriplay.org/genres/youtube/education",     "http://uriplay.org/genres/uriplay/learning");
        genres.put("http://uriplay.org/genres/youtube/entertainment", "http://uriplay.org/genres/uriplay/entertainment");
        genres.put("http://uriplay.org/genres/youtube/film",          "http://uriplay.org/genres/uriplay/film");
        genres.put("http://uriplay.org/genres/youtube/games",         "http://uriplay.org/genres/uriplay/entertainment");
        genres.put("http://uriplay.org/genres/youtube/howto",         "http://uriplay.org/genres/uriplay/lifestyle");
        genres.put("http://uriplay.org/genres/youtube/movies",        "http://uriplay.org/genres/uriplay/film");
        genres.put("http://uriplay.org/genres/youtube/music",         "http://uriplay.org/genres/uriplay/music");
        genres.put("http://uriplay.org/genres/youtube/news",          "http://uriplay.org/genres/uriplay/news");
        genres.put("http://uriplay.org/genres/youtube/nonprofit",     "http://uriplay.org/genres/uriplay/factual");
        genres.put("http://uriplay.org/genres/youtube/people",        "http://uriplay.org/genres/uriplay/entertainment");
        genres.put("http://uriplay.org/genres/youtube/animals",       "http://uriplay.org/genres/uriplay/animals");
        genres.put("http://uriplay.org/genres/youtube/tech",          "http://uriplay.org/genres/uriplay/factual");
        genres.put("http://uriplay.org/genres/youtube/sports",        "http://uriplay.org/genres/uriplay/sports");
        genres.put("http://uriplay.org/genres/youtube/travel",        "http://uriplay.org/genres/uriplay/factual");
     }
}
