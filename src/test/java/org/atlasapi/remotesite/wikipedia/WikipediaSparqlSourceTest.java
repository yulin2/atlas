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

package org.atlasapi.remotesite.wikipedia;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import junit.framework.TestCase;

/**
 * Unit test for {@link WikipediaSparqlSource}.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class WikipediaSparqlSourceTest extends TestCase {

	public void testDbpediaUriFromWikipediaUri() throws Exception {
		WikipediaSparqlSource source = new WikipediaSparqlSource("http://en.wikipedia.org/wiki/The_Poison_Sky");
		source.setCanonicalDbpediaUri("http://dbpedia.org/resource/The_Poison_Sky");
		assertThat(source.getCanonicalWikipediaUri(), is("http://en.wikipedia.org/wiki/The_Poison_Sky"));
	}
	
	public void testExtractsArticleNameFromWikipediaUrl() throws Exception {
		WikipediaSparqlSource source = new WikipediaSparqlSource("http://en.wikipedia.org/wiki/The_Poison_Sky");
		assertThat(source.getCanonicalDbpediaUri(), is("http://dbpedia.org/resource/The_Poison_Sky"));
	}
	
	public void testExtractsArticleNameFromDbpediaPageUrl() throws Exception {
		WikipediaSparqlSource source = new WikipediaSparqlSource("http://dbpedia.org/page/The_Poison_Sky");
		assertThat(source.getCanonicalWikipediaUri(), is("http://en.wikipedia.org/wiki/The_Poison_Sky"));
		assertThat(source.getCanonicalDbpediaUri(), is("http://dbpedia.org/resource/The_Poison_Sky"));
	}
	
	public void testExtractsArticleNameFromDbpediaResourceUrl() throws Exception {
		WikipediaSparqlSource source = new WikipediaSparqlSource("http://dbpedia.org/resource/The_Poison_Sky");
		assertThat(source.getCanonicalWikipediaUri(), is("http://en.wikipedia.org/wiki/The_Poison_Sky"));
		assertThat(source.getCanonicalDbpediaUri(), is("http://dbpedia.org/resource/The_Poison_Sky"));
	}
		

}
