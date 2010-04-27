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

package org.uriplay.remotesite.bbc;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Mapping from the genre strings used in BBC OPML files
 * to genres as defined by BBC /programmes
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BbcPodcastGenreMap {

	private static final Map<String, String> genres = Maps.newHashMap();
	
	private static final String ROOT = "http://www.bbc.co.uk/programmes/genres/";
	
	static {
		
		genres.put("Urban", "music/hiphoprnbanddancehall");
		genres.put("Music", "music");
		genres.put("Sport", "sport");
		genres.put("News & Current Affairs", "news");
		genres.put("Factual", "factual");
		genres.put("Comedy & Quizzes", "entertainmentandcomedy");
		genres.put("Entertainment", "entertainmentandcomedy");
		genres.put("Religion & Ethics", "religionandethics");
		genres.put("Arts & Drama", "factual/artscultureandthemedia");
		genres.put("Classical", "music/classical");
		genres.put("World", "music/world");
		genres.put("Pop & Chart", "music/popandchart");
		genres.put("Rock & Indie", "music/rockandindie");
		genres.put("Science", "factual/sciencenatureandenvironment");
		genres.put("Childrens", "childrens");
		genres.put("History", "factual/history");
		genres.put("Folk & Country", "music/folk");
		genres.put("Jazz", "music/jazzandblues");
		genres.put("Classic Pop & Rock", "music/classicpopandrock");
		genres.put("Music Documentaries", "factual/artscultureandthemedia"); // not in supplied spreadsheet, a guess, for now
		genres.put("Blues Soul & Reggae", "music/soulandreggae");
		genres.put("Dance", "music/danceandelectronica");
		genres.put("Soap", "drama/soaps");
		genres.put("Experimental", "music/world"); // not in supplied spreadsheet, a guess, for now
		genres.put("Desi", "music/desi"); // not in supplied spreadsheet, but obvious
		
	}
	
	public String lookup(String genre) {
		if (genres.get(genre) == null) {
			return null;
		}
		return ROOT + genres.get(genre);
	}

}
