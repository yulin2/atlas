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


import org.atlasapi.genres.AtlasGenre;
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
       genres.put("http://www.channel4.com/programmes/tags/animals",                   AtlasGenre.ANIMALS);
       genres.put("http://www.channel4.com/programmes/tags/animation",                 AtlasGenre.ANIMATION);
       genres.put("http://www.channel4.com/programmes/tags/art-design-and-literature", AtlasGenre.FACTUAL);
       genres.put("http://www.channel4.com/programmes/tags/business-money",            AtlasGenre.FACTUAL);
       genres.put("http://www.channel4.com/programmes/tags/chat-shows",                AtlasGenre.ENTERTAINMENT);
       genres.put("http://www.channel4.com/programmes/tags/childrens-shows", 		   AtlasGenre.CHILDRENS);
       genres.put("http://www.channel4.com/programmes/tags/comedy",                    AtlasGenre.COMEDY);
       genres.put("http://www.channel4.com/programmes/tags/disability",                AtlasGenre.FACTUAL);
       genres.put("http://www.channel4.com/programmes/tags/documentaries",             AtlasGenre.FACTUAL);
       genres.put("http://www.channel4.com/programmes/tags/drama",                     AtlasGenre.DRAMA);
       // ignored: http://www.channel4.com/programmes/tags/e4
       genres.put("http://www.channel4.com/programmes/tags/education-and-learning",    AtlasGenre.LEARNING);
       genres.put("http://www.channel4.com/programmes/tags/entertainment",             AtlasGenre.ENTERTAINMENT);
       genres.put("http://www.channel4.com/programmes/tags/family-and-parenting",      AtlasGenre.LIFESTYLE);
       genres.put("http://www.channel4.com/programmes/tags/fashion-and-beauty",        AtlasGenre.FACTUAL);
       genres.put("http://www.channel4.com/programmes/tags/film",                      AtlasGenre.FILM);
       genres.put("http://www.channel4.com/programmes/tags/food",                      AtlasGenre.LIFESTYLE);
       genres.put("http://www.channel4.com/programmes/tags/health-and-wellbeing",      AtlasGenre.LIFESTYLE);
       genres.put("http://www.channel4.com/programmes/tags/history",                   AtlasGenre.FACTUAL);
       genres.put("http://www.channel4.com/programmes/tags/homes-and-gardens",         AtlasGenre.LIFESTYLE);
       genres.put("http://www.channel4.com/programmes/tags/lifestyle",                 AtlasGenre.LIFESTYLE);
       // ignored: http://www.channel4.com/programmes/tags/more4
       genres.put("http://www.channel4.com/programmes/tags/music",                     AtlasGenre.MUSIC);
       genres.put("http://www.channel4.com/programmes/tags/news-current-affairs-and-politics", AtlasGenre.NEWS);
       genres.put("http://www.channel4.com/programmes/tags/quizzes-and-gameshows",     AtlasGenre.ENTERTAINMENT);
       genres.put("http://www.channel4.com/programmes/tags/reality-shows",             AtlasGenre.ENTERTAINMENT);
       genres.put("http://www.channel4.com/programmes/tags/religion-and-belief",       AtlasGenre.FACTUAL);
       genres.put("http://www.channel4.com/programmes/tags/science-nature-and-the-environment", AtlasGenre.FACTUAL);
       genres.put("http://www.channel4.com/programmes/tags/sex-and-relationships",     AtlasGenre.LIFESTYLE);
       // ignored: http://www.channel4.com/programmes/tags/society-and-culture
       genres.put("http://www.channel4.com/programmes/tags/sports-and-games",          AtlasGenre.SPORT);
       // ignored: http://www.channel4.com/programmes/tags/us-shows
   }
}
