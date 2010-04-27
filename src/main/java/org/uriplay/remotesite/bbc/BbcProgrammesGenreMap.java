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

package org.uriplay.remotesite.bbc;

import java.util.Set;

import org.uriplay.genres.GenreMap;

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
            genres.put("http://www.bbc.co.uk/programmes/genres/childrens", "http://uriplay.org/genres/uriplay/childrens");
            genres.put("http://www.bbc.co.uk/programmes/genres/comedy", "http://uriplay.org/genres/uriplay/comedy");
            genres.put("http://www.bbc.co.uk/programmes/genres/drama", "http://uriplay.org/genres/uriplay/drama");
            genres.put("http://www.bbc.co.uk/programmes/genres/entertainment", "http://uriplay.org/genres/uriplay/entertainment");
            genres.put("http://www.bbc.co.uk/programmes/genres/entertainmentandcomedy", "http://uriplay.org/genres/uriplay/entertainment");
            genres.put("http://www.bbc.co.uk/programmes/genres/learning", "http://uriplay.org/genres/uriplay/learning");
            genres.put("http://www.bbc.co.uk/programmes/genres/music", "http://uriplay.org/genres/uriplay/music");
            genres.put("http://www.bbc.co.uk/programmes/genres/news", "http://uriplay.org/genres/uriplay/news");
            genres.put("http://www.bbc.co.uk/programmes/genres/religionandethics", "http://uriplay.org/genres/uriplay/factual");
            genres.put("http://www.bbc.co.uk/programmes/genres/sport", "http://uriplay.org/genres/uriplay/sports");
            genres.put("http://www.bbc.co.uk/programmes/genres/weather", "http://uriplay.org/genres/uriplay/news");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/antiques", "http://uriplay.org/genres/uriplay/lifestyle");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/artscultureandthemedia", "http://uriplay.org/genres/uriplay/factual");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/beautyandstyle", "http://uriplay.org/genres/uriplay/lifestyle");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/carsandmotors", "http://uriplay.org/genres/uriplay/lifestyle");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/consumer", "http://uriplay.org/genres/uriplay/lifestyle");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/crimeandjustice", "http://uriplay.org/genres/uriplay/factual");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/disability", "http://uriplay.org/genres/uriplay/factual");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/familiesandrelationships", "http://uriplay.org/genres/uriplay/lifestyle");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/foodanddrink", "http://uriplay.org/genres/uriplay/lifestyle");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/healthandwellbeing", "http://uriplay.org/genres/uriplay/lifestyle");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/history", "http://uriplay.org/genres/uriplay/factual");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/homesandgardens", "http://uriplay.org/genres/uriplay/lifestyle");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/lifestories", "http://uriplay.org/genres/uriplay/factual");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/money", "http://uriplay.org/genres/uriplay/factual");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/petsandanimals", "http://uriplay.org/genres/uriplay/animals");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/politics", "http://uriplay.org/genres/uriplay/factual");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/scienceandnature", "http://uriplay.org/genres/uriplay/factual");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/travel", "http://uriplay.org/genres/uriplay/lifestyle");
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
		String mappedGenre = genres.get(sourceGenre.toLowerCase());
		if (mappedGenre != null) { 
			mappedGenres.add(mappedGenre); 
		} else {
			if (sourceGenre.lastIndexOf('/') > -1) {
				convert(sourceGenre.substring(0, sourceGenre.lastIndexOf('/')), mappedGenres);
			}
		}
	}

}
