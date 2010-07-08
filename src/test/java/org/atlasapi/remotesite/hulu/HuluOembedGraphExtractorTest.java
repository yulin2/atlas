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

package org.atlasapi.remotesite.hulu;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.atlasapi.remotesite.hulu.HuluOembedGraphExtractor;

import junit.framework.TestCase;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class HuluOembedGraphExtractorTest extends TestCase {

	public void testExtractsCurie() throws Exception {
		
		HuluOembedGraphExtractor extractor = new HuluOembedGraphExtractor();
		
		assertThat(extractor.curieFor("http://www.hulu.com/watch/37199"), is("hulu:37199"));
		assertThat(extractor.curieFor("http://www.hulu.com/watch/78417/the-daily-show-with-jon-stewart-wed-jun-17-2009"), is("hulu:78417"));
		
	}
}
