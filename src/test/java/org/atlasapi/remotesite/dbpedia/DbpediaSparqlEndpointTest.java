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

package org.atlasapi.remotesite.dbpedia;

import junit.framework.TestCase;

/**
 * Test accessing the DbPedia SPARQL endpoint.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class DbpediaSparqlEndpointTest extends TestCase {

	public void testCanRetrieveResourceByImdbId() throws Exception {
//		
//		String imdbId = "1208127";
//		
//		SparqlQuery query = 
//			SparqlQuery.select("dbpedia_resource").whereSubjectOf("dbpprop:imdbId ", imdbId).withPrefix("dbpprop", "http://dbpedia.org/property/");
//		
//		ResultSet result = new DbpediaSparqlEndpoint().execute(query);
//		
//		Resource resource = resourceFrom(query, result);
//		
//		assertEquals("The_Poison_Sky", resource.getLocalName());
//		assertEquals("http://dbpedia.org/resource/The_Poison_Sky", resource.getURI());
	}

//	private Resource resourceFrom(SparqlQuery query, ResultSet result) {
//		assertTrue(result.hasNext());
//	
//		ResultBinding item = (ResultBinding) result.next();
//		Resource resource = item.getResource(query.getSelectId());
//		return resource;
//	}
	
	public void testCanSelectRedirectUri() throws Exception {
		
//		String resourceUri = "http://dbpedia.org/resource/The_West_Wing_%28TV_series%29";
//		
//		SparqlQuery query = 
//			SparqlQuery.selectDistinct("uri").whereObjectOf(resourceUri, "dbpp:redirect").withPrefix("dbpp", "http://dbpedia.org/property/");
//	
//		ResultSet result = new DbpediaSparqlEndpoint().execute(query);
//		
//		Resource resource = resourceFrom(query, result);
//		
//		assertEquals("The_West_Wing", resource.getLocalName());
//		assertEquals("http://dbpedia.org/resource/The_West_Wing", resource.getURI());
	}
}

