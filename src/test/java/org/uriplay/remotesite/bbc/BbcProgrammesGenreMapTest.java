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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import java.util.Set;

import junit.framework.TestCase;

import com.google.common.collect.Sets;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BbcProgrammesGenreMapTest extends TestCase {
	
	public void testMatchesOnSubUrlsIfWholeUrlDoesNotMatch() throws Exception {
		
		Set<String> sourceGenres = Sets.newHashSet("http://www.bbc.co.uk/programmes/genres/childrens/entertainmentandcomedy");
		Set<String> mappedGenres = new BbcProgrammesGenreMap().map(sourceGenres);
		
		assertThat(mappedGenres.size(), is(2));
		assertThat(mappedGenres, hasItems("http://www.bbc.co.uk/programmes/genres/childrens/entertainmentandcomedy", "http://uriplay.org/genres/uriplay/childrens"));
	}
	
	public void testDoesAlterInputIfNoMatchFoundAtAll() throws Exception {
		
		Set<String> sourceGenres = Sets.newHashSet("http://www.bbc.co.uk/programmes/genres/something/unknown");
		Set<String> mappedGenres = new BbcProgrammesGenreMap().map(sourceGenres);
		
		assertThat(mappedGenres.size(), is(1));
		assertThat(mappedGenres, hasItems("http://www.bbc.co.uk/programmes/genres/something/unknown"));
	}
	
	// Note: these mappings also apply for sub-genres, e.g.:  
    // http://www.bbc.co.uk/programmes/genres/childrens/entertainmentandcomedy is as:
    // http://www.bbc.co.uk/programmes/genres/childrens
    // i.e., match on the start of the URI, not the whole URI
}
