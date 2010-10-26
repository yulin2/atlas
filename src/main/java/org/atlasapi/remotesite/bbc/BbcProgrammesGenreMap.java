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

package org.atlasapi.remotesite.bbc;

import java.util.Set;

import org.atlasapi.genres.AtlasGenre;
import org.atlasapi.genres.GenreMap;

import com.google.common.collect.Sets;

/**
 * Mapping from BBC genre uris to URIplay genre uris.
 *  
 * @author Chris Jackson (chris@metabroadcast.com)
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BbcProgrammesGenreMap extends GenreMap {
    
    // Note: these mappings also apply for sub-genres, e.g.:  
    // http://www.bbc.co.uk/programmes/genres/childrens/entertainmentandcomedy is as:
    // http://www.bbc.co.uk/programmes/genres/childrens
    // i.e., match on the start of the URI, not the whole URI

    static {
            genres.put("http://www.bbc.co.uk/programmes/genres/childrens", AtlasGenre.CHILDRENS);
            genres.put("http://www.bbc.co.uk/programmes/genres/comedy", AtlasGenre.COMEDY);
            genres.put("http://www.bbc.co.uk/programmes/genres/drama", AtlasGenre.DRAMA);
            genres.put("http://www.bbc.co.uk/programmes/genres/entertainment", AtlasGenre.ENTERTAINMENT);
            genres.put("http://www.bbc.co.uk/programmes/genres/entertainmentandcomedy", AtlasGenre.ENTERTAINMENT);
            genres.put("http://www.bbc.co.uk/programmes/genres/learning", AtlasGenre.LEARNING);
            genres.put("http://www.bbc.co.uk/programmes/genres/music", AtlasGenre.MUSIC);
            genres.put("http://www.bbc.co.uk/programmes/genres/news", AtlasGenre.NEWS);
            genres.put("http://www.bbc.co.uk/programmes/genres/religionandethics", AtlasGenre.FACTUAL);
            genres.put("http://www.bbc.co.uk/programmes/genres/sport", AtlasGenre.SPORT);
            genres.put("http://www.bbc.co.uk/programmes/genres/weather", AtlasGenre.NEWS);
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/antiques", AtlasGenre.LIFESTYLE);
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/artscultureandthemedia", AtlasGenre.FACTUAL);
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/beautyandstyle", AtlasGenre.LIFESTYLE);
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/carsandmotors", AtlasGenre.LIFESTYLE);
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/consumer", AtlasGenre.LIFESTYLE);
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/crimeandjustice", AtlasGenre.FACTUAL);
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/disability", AtlasGenre.FACTUAL);
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/familiesandrelationships", AtlasGenre.LIFESTYLE);
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/foodanddrink", AtlasGenre.LIFESTYLE);
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/healthandwellbeing", AtlasGenre.LIFESTYLE);
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/history", AtlasGenre.FACTUAL);
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/homesandgardens", AtlasGenre.LIFESTYLE);
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/lifestories", AtlasGenre.FACTUAL);
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/money", AtlasGenre.FACTUAL);
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/petsandanimals", AtlasGenre.ANIMALS);
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/politics", AtlasGenre.FACTUAL);
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/scienceandnature", AtlasGenre.FACTUAL);
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/travel", AtlasGenre.LIFESTYLE);
    }
    
    @Override
    public Set<String> map(Set<String> sourceGenres) {
		
		Set<String> mappedGenres = Sets.newHashSet();
		
		for (String sourceGenre : sourceGenres) {
			convert(sourceGenre, mappedGenres);
		}
		
		mappedGenres.addAll(sourceGenres);
		
		return mappedGenres;
	}

	private void convert(String sourceGenre, Set<String> mappedGenres) {
		AtlasGenre mappedGenre = genres.get(sourceGenre.toLowerCase());
		if (mappedGenre != null) { 
			mappedGenres.add(mappedGenre.getUri()); 
		} else {
			if (sourceGenre.lastIndexOf('/') > -1) {
				convert(sourceGenre.substring(0, sourceGenre.lastIndexOf('/')), mappedGenres);
			}
		}
	}

}
