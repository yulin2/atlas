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

package org.atlasapi.remotesite.imdb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.atlasapi.remotesite.imdb.ImdbSource;

import junit.framework.TestCase;

/**
 * Unit test for {@link ImdbSource}.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class ImdbSourceTest extends TestCase {

	public void testExtractsArticleNameFromImdbUrl() throws Exception {
		
		ImdbSource source = new ImdbSource(null, "http://www.imdb.com/title/tt0200276");
		assertThat(source.getImdbId(), is("0200276"));
	}
	
	public void testExtractsArticleNameFromImdbUrlWithTrailingSlash() throws Exception {
		
		ImdbSource source = new ImdbSource(null, "http://www.imdb.com/title/tt0200276/");
		assertThat(source.getImdbId(), is("0200276"));
	} 

	public void testExtractsArticleNameFromImdbUrlWithSuffix() throws Exception {
		
		ImdbSource source = new ImdbSource(null, "http://www.imdb.com/title/tt0200276/quotes");
		assertThat(source.getImdbId(), is("0200276"));
	}
	
	public void testCreatesDbpediaSparqlQuery() throws Exception {
		
		ImdbSource source = new ImdbSource(null, "http://www.imdb.com/title/tt0200276/");
		String query = source.getSparqlQuery().toString();
		assertThat(query, containsString("PREFIX dbpprop: <http://dbpedia.org/property/>"));
		assertThat(query, containsString("SELECT ?dbpedia_resource"));
		assertThat(query, containsString("WHERE { ?dbpedia_resource dbpprop:imdbId 0200276. }"));
	}
	
	
		

}
