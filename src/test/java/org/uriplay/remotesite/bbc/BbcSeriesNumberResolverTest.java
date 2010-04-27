/* Copyright 2010 Meta Broadcast Ltd

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
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.jherd.remotesite.http.RemoteSiteClient;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.springframework.core.io.ClassPathResource;

@SuppressWarnings("unchecked")
public class BbcSeriesNumberResolverTest extends MockObjectTestCase {

	public void testTheResolver() throws Exception {
		final String seriesUri = "http://www.bbc.co.uk/programmes/b007q8vv";
		final RemoteSiteClient<Reader> client = mock(RemoteSiteClient.class);
		
		checking(new Expectations() {{
			one(client).get(seriesUri + ".rdf"); will(returnValue(xmlDocument()));
		}});
		
		SeriesFetchingBbcSeriesNumberResolver resolver = new SeriesFetchingBbcSeriesNumberResolver(client);
		
		assertThat(resolver.seriesNumberFor(seriesUri).requireValue(), is(Integer.valueOf(6)));
		
		// first request should have populated the cache
		assertThat(resolver.seriesNumberFor(seriesUri).requireValue(), is(Integer.valueOf(6)));
	}
	
	public void testLookupingUpAMissingSeries() throws Exception {
		final String missingSeriesUri = "http://www.bbc.co.uk/programmes/b00missing";
		final RemoteSiteClient<Reader> client = mock(RemoteSiteClient.class);
		
		checking(new Expectations() {{
			one(client).get(missingSeriesUri + ".rdf"); will(returnValue(new StringReader("this string doesn't contain a series number")));
		}});
		
		SeriesFetchingBbcSeriesNumberResolver resolver = new SeriesFetchingBbcSeriesNumberResolver(client);
		
		assertTrue(resolver.seriesNumberFor(missingSeriesUri).isNothing());
		
		// first request should have populated the cache (negative caching enabled)
		assertTrue(resolver.seriesNumberFor(missingSeriesUri).isNothing());
	}
	
	protected Reader xmlDocument() throws IOException {
		return new InputStreamReader(new ClassPathResource("top-gear-series-rdf.xml").getInputStream());
	}
}
