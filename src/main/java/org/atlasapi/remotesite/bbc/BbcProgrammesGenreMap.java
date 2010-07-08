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
            genres.put("http://www.bbc.co.uk/programmes/genres/childrens", "http://ref.atlasapi.org/genres/atlas/childrens");
            genres.put("http://www.bbc.co.uk/programmes/genres/comedy", "http://ref.atlasapi.org/genres/atlas/comedy");
            genres.put("http://www.bbc.co.uk/programmes/genres/drama", "http://ref.atlasapi.org/genres/atlas/drama");
            genres.put("http://www.bbc.co.uk/programmes/genres/entertainment", "http://ref.atlasapi.org/genres/atlas/entertainment");
            genres.put("http://www.bbc.co.uk/programmes/genres/entertainmentandcomedy", "http://ref.atlasapi.org/genres/atlas/entertainment");
            genres.put("http://www.bbc.co.uk/programmes/genres/learning", "http://ref.atlasapi.org/genres/atlas/learning");
            genres.put("http://www.bbc.co.uk/programmes/genres/music", "http://ref.atlasapi.org/genres/atlas/music");
            genres.put("http://www.bbc.co.uk/programmes/genres/news", "http://ref.atlasapi.org/genres/atlas/news");
            genres.put("http://www.bbc.co.uk/programmes/genres/religionandethics", "http://ref.atlasapi.org/genres/atlas/factual");
            genres.put("http://www.bbc.co.uk/programmes/genres/sport", "http://ref.atlasapi.org/genres/atlas/sports");
            genres.put("http://www.bbc.co.uk/programmes/genres/weather", "http://ref.atlasapi.org/genres/atlas/news");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/antiques", "http://ref.atlasapi.org/genres/atlas/lifestyle");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/artscultureandthemedia", "http://ref.atlasapi.org/genres/atlas/factual");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/beautyandstyle", "http://ref.atlasapi.org/genres/atlas/lifestyle");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/carsandmotors", "http://ref.atlasapi.org/genres/atlas/lifestyle");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/consumer", "http://ref.atlasapi.org/genres/atlas/lifestyle");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/crimeandjustice", "http://ref.atlasapi.org/genres/atlas/factual");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/disability", "http://ref.atlasapi.org/genres/atlas/factual");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/familiesandrelationships", "http://ref.atlasapi.org/genres/atlas/lifestyle");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/foodanddrink", "http://ref.atlasapi.org/genres/atlas/lifestyle");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/healthandwellbeing", "http://ref.atlasapi.org/genres/atlas/lifestyle");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/history", "http://ref.atlasapi.org/genres/atlas/factual");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/homesandgardens", "http://ref.atlasapi.org/genres/atlas/lifestyle");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/lifestories", "http://ref.atlasapi.org/genres/atlas/factual");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/money", "http://ref.atlasapi.org/genres/atlas/factual");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/petsandanimals", "http://ref.atlasapi.org/genres/atlas/animals");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/politics", "http://ref.atlasapi.org/genres/atlas/factual");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/scienceandnature", "http://ref.atlasapi.org/genres/atlas/factual");
            genres.put("http://www.bbc.co.uk/programmes/genres/factual/travel", "http://ref.atlasapi.org/genres/atlas/lifestyle");
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
