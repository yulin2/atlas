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

package org.uriplay.remotesite.sparql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import junit.framework.TestCase;

/**
 * Unit test for {@link SparqlQuery}.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class SparqlQueryTest extends TestCase {

	public void testCanSpecifySelect() throws Exception {
		
		SparqlQuery query = SparqlQuery.select("x").whereSubjectOf("y", "123");
		assertThat(query.toString(), is("SELECT ?x WHERE { ?x y 123. }"));
	}
	
	public void testCanSpecifyPrefixes() throws Exception {
		
		SparqlQuery query = SparqlQuery.select("x").whereSubjectOf("prefix:y", "123").withPrefix("prefix", "http://example.com/prefix/");
		assertThat(query.toString(), is("PREFIX prefix: <http://example.com/prefix/> SELECT ?x WHERE { ?x prefix:y 123. }"));
	}
	
	public void testCanSelectDistinctItems() throws Exception {
		
		SparqlQuery query = SparqlQuery.selectDistinct("x").whereSubjectOf("y", "123");
		assertThat(query.toString(), is("SELECT DISTINCT ?x WHERE { ?x y 123. }"));
	}
	
	public void testCanSetCustomQuery() throws Exception {
		
		SparqlQuery query = SparqlQuery.fromString("whatever I like");
		assertThat(query.toString(), is("whatever I like"));
	}
}
