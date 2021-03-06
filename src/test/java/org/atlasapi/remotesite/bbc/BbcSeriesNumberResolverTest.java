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

package org.atlasapi.remotesite.bbc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;

import com.metabroadcast.common.http.SimpleHttpClient;

@RunWith(JMock.class)
public class BbcSeriesNumberResolverTest extends TestCase {

    private final Mockery context = new Mockery();
	final SimpleHttpClient client = context.mock(SimpleHttpClient.class);

    @Test
	public void testTheResolver() throws Exception {
		final String seriesUri = "http://www.bbc.co.uk/programmes/b007q8vv";
		
		context.checking(new Expectations() {{
			one(client).getContentsOf(seriesUri + ".rdf"); will(returnValue(xmlDocument()));
		}});
		
		SeriesFetchingBbcSeriesNumberResolver resolver = new SeriesFetchingBbcSeriesNumberResolver(client);
		
		assertThat(resolver.seriesNumberFor(seriesUri).requireValue(), is(Integer.valueOf(6)));
		
		// first request should have populated the cache
		assertThat(resolver.seriesNumberFor(seriesUri).requireValue(), is(Integer.valueOf(6)));
	}

    @Test
	public void testLookupingUpAMissingSeries() throws Exception {
		final String missingSeriesUri = "http://www.bbc.co.uk/programmes/b00missing";
		
		context.checking(new Expectations() {{
			one(client).getContentsOf(missingSeriesUri + ".rdf"); will(returnValue("this string doesn't contain a series number"));
		}});
		
		SeriesFetchingBbcSeriesNumberResolver resolver = new SeriesFetchingBbcSeriesNumberResolver(client);
		
		assertTrue(resolver.seriesNumberFor(missingSeriesUri).isNothing());
		
		// first request should have populated the cache (negative caching enabled)
		assertTrue(resolver.seriesNumberFor(missingSeriesUri).isNothing());
	}
	
	protected String xmlDocument() throws IOException {
		return IOUtils.toString(new ClassPathResource("top-gear-series-rdf.xml").getInputStream());
	}
}
