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

package org.uriplay.remotesite.wikipedia;

import static org.uriplay.remotesite.wikipedia.Constants.TELEVISION_SHOW_TYPE_URL;
import static org.uriplay.remotesite.wikipedia.Constants.WEST_WING_DBPEDIA_URI;
import static org.uriplay.remotesite.wikipedia.Constants.WEST_WING_WIKIPEDIA_URI;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.uriplay.media.entity.Brand;
import org.uriplay.persistence.system.RequestTimer;
import org.uriplay.remotesite.ContentExtractor;
import org.uriplay.remotesite.sparql.SparqlEndpoint;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * Unit test for {@link WikipediaSparqlAdapter}.
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@SuppressWarnings("unchecked")
public class WikipediaSparqlAdapterTest extends MockObjectTestCase {

	WikipediaSparqlAdapter adapter;
	SparqlEndpoint sparqlEndpoint = mock(SparqlEndpoint.class);
	ContentExtractor<WikipediaSparqlSource, org.uriplay.media.entity.Description> propertyExtractor = mock(ContentExtractor.class);
	ResultSet resultSet = mock(ResultSet.class);
	Binding binding = mock(Binding.class);
	Model model = mock(Model.class);
	ResultBinding resultBinding = new ResultBinding(model, binding);
	WikipediaSparqlSource wikipediaSource = new WikipediaSparqlSource(WEST_WING_WIKIPEDIA_URI);
	Resource canonicalResource = new ResourceImpl(WEST_WING_DBPEDIA_URI);
	Resource typeResource = new ResourceImpl(TELEVISION_SHOW_TYPE_URL);

	RequestTimer timer = mock(RequestTimer.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		wikipediaSource.setCanonicalDbpediaUri(WEST_WING_DBPEDIA_URI);
		adapter = new WikipediaSparqlAdapter(sparqlEndpoint, propertyExtractor);
		
		checking(new Expectations() {{
			ignoring(timer);
		}});
	}
	
	public void testPerformsTelevisionShowQuery() throws Exception {
		final Sequence querySequence = sequence("ItemQuerySequence");
		
		checking(new Expectations() {{
			allowing(resultSet).next(); will(returnValue(resultBinding));
			
			one(sparqlEndpoint).execute(wikipediaSource.getUriQuery()); inSequence(querySequence); will(returnValue(resultSet));
			one(resultSet).hasNext(); inSequence(querySequence); will(returnValue(true));
			allowing(binding).get(Var.alloc(wikipediaSource.getUriQuery().getSelectId())); inSequence(querySequence); will(returnValue(canonicalResource.asNode()));
			allowing(model).asRDFNode(canonicalResource.asNode()); inSequence(querySequence); will(returnValue(canonicalResource));
			
			one(sparqlEndpoint).execute(wikipediaSource.getItemTypeQuery()); inSequence(querySequence); will(returnValue(resultSet));
			one(resultSet).hasNext(); inSequence(querySequence); will(returnValue(true));
			allowing(binding).get(Var.alloc(WikipediaSparqlSource.TYPE_ID)); inSequence(querySequence); will(returnValue(typeResource.asNode()));
			allowing(model).asRDFNode(typeResource.asNode()); inSequence(querySequence); will(returnValue(typeResource));
			one(resultSet).hasNext(); inSequence(querySequence); will(returnValue(false));
			
			one(sparqlEndpoint).execute(wikipediaSource.getItemQuery()); inSequence(querySequence); will(returnValue(resultSet));			
			one(sparqlEndpoint).execute(wikipediaSource.getContainedInQuery()); inSequence(querySequence); will(returnValue(resultSet));			
			one(sparqlEndpoint).execute(wikipediaSource.getChildTypesOfTVShowQuery()); inSequence(querySequence); will(returnValue(resultSet));			
			one(sparqlEndpoint).execute(wikipediaSource.getChildrenOfTVShowQuery()); inSequence(querySequence); will(returnValue(resultSet));			

			one(propertyExtractor).extract(with(configuredSource())); will(returnValue(new Brand()));
		}});
		
		adapter.fetch(WEST_WING_WIKIPEDIA_URI, timer);
	}
	
	public void testCanFetchDbpediaUris() throws Exception {
		assertTrue(adapter.canFetch("http://dbpedia.org/page/Silence_in_the_Library"));
		assertTrue(adapter.canFetch("http://dbpedia.org/resource/Silence_in_the_Library"));
	}

	public void testCanFetchWikipediaUris() throws Exception {
		assertTrue(adapter.canFetch("http://en.wikipedia.org/wiki/Silence_in_the_Library"));
		assertTrue(adapter.canFetch("http://en.wikipedia.org/wiki/Poison_Sky"));
	}

	public void testWillNotFetchBbcUris() throws Exception {
		assertFalse(adapter.canFetch("http://bbc.co.uk/programmes/Silence_in_the_Library"));
	}
	
	protected Matcher<WikipediaSparqlSource> configuredSource() {
		return new TypeSafeMatcher<WikipediaSparqlSource>() {

			@Override
			public boolean matchesSafely(WikipediaSparqlSource item) {
				return item.getCanonicalDbpediaUri().equals(WEST_WING_DBPEDIA_URI) 
					&& item.getRootProperties().equals(resultSet) && item.getContainedInProperties().equals(resultSet);
			}

			public void describeTo(Description description) {
				
			}};
	}
}
