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

package org.atlasapi.remotesite.channel4;


import org.atlasapi.genres.GenreMap;


/**
 * Mapping from YouTube's genre uris to URIplay genre uris.
 *  
 * @author Chris Jackson (chris@metabroadcast.com)
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class C4GenreMap extends GenreMap {

   // Some genres are 'ignored' (not mapped to a atlas genre) because they span multiple atlas genres
   // Since C4 uses multiple genres per programme, it is likely that each item will still be mapped to a 
   //  meaningful URIplay genre.

   static {
       genres.put("http://www.channel4.com/programmes/tags/animals",                   "http://ref.atlasapi.org/genres/atlas/animals");
       genres.put("http://www.channel4.com/programmes/tags/animation",                 "http://ref.atlasapi.org/genres/atlas/film");
       genres.put("http://www.channel4.com/programmes/tags/art-design-and-literature", "http://ref.atlasapi.org/genres/atlas/factual");
       genres.put("http://www.channel4.com/programmes/tags/business-money",            "http://ref.atlasapi.org/genres/atlas/factual");
       genres.put("http://www.channel4.com/programmes/tags/chat-shows",                "http://ref.atlasapi.org/genres/atlas/entertainment");
       genres.put("http://www.channel4.com/programmes/tags/childrens-shows", 		   "http://ref.atlasapi.org/genres/atlas/childrens");
       genres.put("http://www.channel4.com/programmes/tags/comedy",                    "http://ref.atlasapi.org/genres/atlas/comedy");
       genres.put("http://www.channel4.com/programmes/tags/disability",                "http://ref.atlasapi.org/genres/atlas/factual");
       genres.put("http://www.channel4.com/programmes/tags/documentaries",             "http://ref.atlasapi.org/genres/atlas/factual");
       genres.put("http://www.channel4.com/programmes/tags/drama",                     "http://ref.atlasapi.org/genres/atlas/drama");
       // ignored: http://www.channel4.com/programmes/tags/e4
       genres.put("http://www.channel4.com/programmes/tags/education-and-learning",    "http://ref.atlasapi.org/genres/atlas/learning");
       genres.put("http://www.channel4.com/programmes/tags/entertainment",             "http://ref.atlasapi.org/genres/atlas/entertainment");
       genres.put("http://www.channel4.com/programmes/tags/family-and-parenting",      "http://ref.atlasapi.org/genres/atlas/lifestyle");
       genres.put("http://www.channel4.com/programmes/tags/fashion-and-beauty",        "http://ref.atlasapi.org/genres/atlas/factual");
       genres.put("http://www.channel4.com/programmes/tags/film",                      "http://ref.atlasapi.org/genres/atlas/film");
       genres.put("http://www.channel4.com/programmes/tags/food",                      "http://ref.atlasapi.org/genres/atlas/lifestyle");
       genres.put("http://www.channel4.com/programmes/tags/health-and-wellbeing",      "http://ref.atlasapi.org/genres/atlas/lifestyle");
       genres.put("http://www.channel4.com/programmes/tags/history",                   "http://ref.atlasapi.org/genres/atlas/factual");
       genres.put("http://www.channel4.com/programmes/tags/homes-and-gardens",         "http://ref.atlasapi.org/genres/atlas/lifestyle");
       genres.put("http://www.channel4.com/programmes/tags/lifestyle",                 "http://ref.atlasapi.org/genres/atlas/lifestyle");
       // ignored: http://www.channel4.com/programmes/tags/more4
       genres.put("http://www.channel4.com/programmes/tags/music",                     "http://ref.atlasapi.org/genres/atlas/music");
       genres.put("http://www.channel4.com/programmes/tags/news-current-affairs-and-politics", "http://ref.atlasapi.org/genres/atlas/news");
       genres.put("http://www.channel4.com/programmes/tags/quizzes-and-gameshows",     "http://ref.atlasapi.org/genres/atlas/entertainment");
       genres.put("http://www.channel4.com/programmes/tags/reality-shows",             "http://ref.atlasapi.org/genres/atlas/entertainment");
       genres.put("http://www.channel4.com/programmes/tags/religion-and-belief",       "http://ref.atlasapi.org/genres/atlas/factual");
       genres.put("http://www.channel4.com/programmes/tags/science-nature-and-the-environment", "http://ref.atlasapi.org/genres/atlas/factual");
       genres.put("http://www.channel4.com/programmes/tags/sex-and-relationships",     "http://ref.atlasapi.org/genres/atlas/lifestyle");
       // ignored: http://www.channel4.com/programmes/tags/society-and-culture
       genres.put("http://www.channel4.com/programmes/tags/sports-and-games",          "http://ref.atlasapi.org/genres/atlas/sports");
       // ignored: http://www.channel4.com/programmes/tags/us-shows
   }
}
