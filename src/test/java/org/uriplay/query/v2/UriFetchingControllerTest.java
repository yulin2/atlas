///* Copyright 2009 British Broadcasting Corporation
//   Copyright 2009 Meta Broadcast Ltd
//
//Licensed under the Apache License, Version 2.0 (the "License"); you
//may not use this file except in compliance with the License. You may
//obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
//implied. See the License for the specific language governing
//permissions and limitations under the License. */

package org.uriplay.query.v2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.jherd.core.Factory;
import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.Fetcher;
import org.jherd.remotesite.NoMatchingAdapterException;
import org.jherd.remotesite.timing.RequestTimer;
import org.jherd.servlet.ContentNotFoundException;
import org.jherd.servlet.RequestNs;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.uriplay.beans.ProjectionException;
import org.uriplay.beans.Projector;
import org.uriplay.media.entity.Description;
import org.uriplay.media.entity.Item;
import org.uriplay.persistence.testing.DummyContentData;

import com.google.common.collect.Sets;

/**
 * Unit test for {@link UriFetchingController}.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author Lee Denison (lee@metabroadcast.com)
 */
@SuppressWarnings("unchecked")
public class UriFetchingControllerTest extends MockObjectTestCase {

	static final Item item = new DummyContentData().eggsForBreakfast;
	
	static final String URI = item.getCanonicalUri();

	MockHttpServletRequest request = new MockHttpServletRequest();
	MockHttpServletResponse response = new MockHttpServletResponse();

	Fetcher<Description> executor;
	Projector projector;
	
	Factory<RequestTimer> timerFactory;
	RequestTimer timer;

	UriFetchingController controller;
	
	Set<Object> filteredBeans = (Set) Sets.newHashSet(new Object());
	Set<Object> projectedBeans = (Set) Sets.newHashSet(new Object());
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		timerFactory = mock(Factory.class);
		timer = mock(RequestTimer.class);
		projector = mock(Projector.class);
		controller = new UriFetchingController(executor, projector, timerFactory);

		request.setMethod("GET");
		request.setParameter("uri", URI);
	}
	
	public void testQueriesFetcherForUri() throws Exception {
		
		checking(new Expectations() {{ 
			one(executor).fetch(URI, timer); will(returnValue(item));
			allowing(timerFactory).create(); will(returnValue(timer));
			one(projector).applyTo(filteredBeans); will(returnValue(projectedBeans));
			ignoring(timer);
		}});
		
		ModelAndView modelAndView = controller.handle(request, response, false);
		
		assertEquals(projectedBeans, modelAndView.getModel().get(RequestNs.GRAPH));
	}
	
	public void testTimesRequestFromController() throws Exception {
		
		checking(new Expectations() {{ 
			one(executor).fetch(URI, timer); will(returnValue(item));
			one(projector).applyTo(filteredBeans); will(returnValue(projectedBeans));
			one(timerFactory).create(); will(returnValue(timer));
			one(timer).start(controller, URI);
			one(timer).nest();
			one(timer).unnest();
			one(timer).stop(controller, URI);
			one(timer).outputTo(response);
		}});
		
		controller.handle(request, response, true);
	}
	
	public void testAppliesProjectorToBeanGraphBeforeRenderingView() throws Exception {
		
		checking(new Expectations() {{ 
			one(executor).fetch(URI, timer); will(returnValue(item));
			one(projector).applyTo(filteredBeans); will(returnValue(projectedBeans));
			allowing(timerFactory).create(); will(returnValue(timer));
			ignoring(timer);
		}});
		
		controller.handle(request, response, false);
	}
	
	public void testReturns404IfNoAdapterMatchesQueryUri() throws Exception {
		
		checking(new Expectations() {{ 
			one(executor).fetch(URI, timer); will(throwException(new NoMatchingAdapterException("")));
			allowing(timerFactory).create(); will(returnValue(timer));
			ignoring(timer);
		}});
		
		try {
			controller.handle(request, response, false);
			fail("Exception expected");
		} catch (ContentNotFoundException cnfe) {
			assertThat(cnfe.getStatusCode(), is(HttpServletResponse.SC_NOT_FOUND));
		}
	}
	
	public void testReturns404OnFetchException() throws Exception {
		
		checking(new Expectations() {{ 
			one(executor).fetch(URI, timer); will(throwException(new FetchException("")));
			allowing(timerFactory).create(); will(returnValue(timer));
			ignoring(timer);
		}});
		
		try {
			controller.handle(request, response, false);
			fail("Exception expected");
		} catch (ContentNotFoundException cnfe) {
			assertThat(cnfe.getStatusCode(), is(HttpServletResponse.SC_NOT_FOUND));
		}
	}
	
	public void testReturns404OnProjectorException() throws Exception {
		
		checking(new Expectations() {{ 
			allowing(executor).fetch(URI, timer); will(returnValue(item));
			allowing(timerFactory).create(); will(returnValue(timer));
			ignoring(timer);
			one(projector).applyTo(filteredBeans); will(throwException(new ProjectionException("no root element")));
		}});
		
		try {
			controller.handle(request, response, false);
			fail("Excpected exception");
		} catch (ContentNotFoundException cnfe) {
			assertThat(cnfe.getStatusCode(), is(HttpServletResponse.SC_NOT_FOUND));
		}
	}
	
	public void testAllowsParamToSuppressTimingInformation() throws Exception {

		checking(new Expectations() {{ 
			one(executor).fetch(URI, timer); will(returnValue(item));
			one(projector).applyTo(filteredBeans);
			allowing(timerFactory).create(); will(returnValue(timer));
			allowing(timer).nest();
			allowing(timer).start(with(anything()), with(any(String.class)));
			allowing(timer).stop(with(anything()), with(any(String.class)));
			allowing(timer).unnest();
			never(timer).outputTo(response);
		}});
		
		controller.handle(request, response, false);
	}
	
}
