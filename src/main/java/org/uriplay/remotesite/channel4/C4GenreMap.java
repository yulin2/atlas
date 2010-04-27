/* Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.uriplay.remotesite.channel4;


import org.uriplay.genres.GenreMap;


/**
 * Mapping from YouTube's genre uris to URIplay genre uris.
 *  
 * @author Chris Jackson (chris@metabroadcast.com)
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class C4GenreMap extends GenreMap {

   // Some genres are 'ignored' (not mapped to a uriplay genre) because they span multiple uriplay genres
   // Since C4 uses multiple genres per programme, it is likely that each item will still be mapped to a 
   //  meaningful URIplay genre.

   static {
       genres.put("http://www.channel4.com/programmes/tags/animals",                   "http://uriplay.org/genres/uriplay/animals");
       genres.put("http://www.channel4.com/programmes/tags/animation",                 "http://uriplay.org/genres/uriplay/film");
       genres.put("http://www.channel4.com/programmes/tags/art-design-and-literature", "http://uriplay.org/genres/uriplay/factual");
       genres.put("http://www.channel4.com/programmes/tags/business-money",            "http://uriplay.org/genres/uriplay/factual");
       genres.put("http://www.channel4.com/programmes/tags/chat-shows",                "http://uriplay.org/genres/uriplay/entertainment");
       genres.put("http://www.channel4.com/programmes/tags/childrens-shows", 		   "http://uriplay.org/genres/uriplay/childrens");
       genres.put("http://www.channel4.com/programmes/tags/comedy",                    "http://uriplay.org/genres/uriplay/comedy");
       genres.put("http://www.channel4.com/programmes/tags/disability",                "http://uriplay.org/genres/uriplay/factual");
       genres.put("http://www.channel4.com/programmes/tags/documentaries",             "http://uriplay.org/genres/uriplay/factual");
       genres.put("http://www.channel4.com/programmes/tags/drama",                     "http://uriplay.org/genres/uriplay/drama");
       // ignored: http://www.channel4.com/programmes/tags/e4
       genres.put("http://www.channel4.com/programmes/tags/education-and-learning",    "http://uriplay.org/genres/uriplay/learning");
       genres.put("http://www.channel4.com/programmes/tags/entertainment",             "http://uriplay.org/genres/uriplay/entertainment");
       genres.put("http://www.channel4.com/programmes/tags/family-and-parenting",      "http://uriplay.org/genres/uriplay/lifestyle");
       genres.put("http://www.channel4.com/programmes/tags/fashion-and-beauty",        "http://uriplay.org/genres/uriplay/factual");
       genres.put("http://www.channel4.com/programmes/tags/film",                      "http://uriplay.org/genres/uriplay/film");
       genres.put("http://www.channel4.com/programmes/tags/food",                      "http://uriplay.org/genres/uriplay/lifestyle");
       genres.put("http://www.channel4.com/programmes/tags/health-and-wellbeing",      "http://uriplay.org/genres/uriplay/lifestyle");
       genres.put("http://www.channel4.com/programmes/tags/history",                   "http://uriplay.org/genres/uriplay/factual");
       genres.put("http://www.channel4.com/programmes/tags/homes-and-gardens",         "http://uriplay.org/genres/uriplay/lifestyle");
       genres.put("http://www.channel4.com/programmes/tags/lifestyle",                 "http://uriplay.org/genres/uriplay/lifestyle");
       // ignored: http://www.channel4.com/programmes/tags/more4
       genres.put("http://www.channel4.com/programmes/tags/music",                     "http://uriplay.org/genres/uriplay/music");
       genres.put("http://www.channel4.com/programmes/tags/news-current-affairs-and-politics", "http://uriplay.org/genres/uriplay/news");
       genres.put("http://www.channel4.com/programmes/tags/quizzes-and-gameshows",     "http://uriplay.org/genres/uriplay/entertainment");
       genres.put("http://www.channel4.com/programmes/tags/reality-shows",             "http://uriplay.org/genres/uriplay/entertainment");
       genres.put("http://www.channel4.com/programmes/tags/religion-and-belief",       "http://uriplay.org/genres/uriplay/factual");
       genres.put("http://www.channel4.com/programmes/tags/science-nature-and-the-environment", "http://uriplay.org/genres/uriplay/factual");
       genres.put("http://www.channel4.com/programmes/tags/sex-and-relationships",     "http://uriplay.org/genres/uriplay/lifestyle");
       // ignored: http://www.channel4.com/programmes/tags/society-and-culture
       genres.put("http://www.channel4.com/programmes/tags/sports-and-games",          "http://uriplay.org/genres/uriplay/sports");
       // ignored: http://www.channel4.com/programmes/tags/us-shows
   }
}
