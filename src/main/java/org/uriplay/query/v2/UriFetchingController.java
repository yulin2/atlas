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

package org.uriplay.query.v2;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jherd.core.Factory;
import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.Fetcher;
import org.jherd.remotesite.NoMatchingAdapterException;
import org.jherd.remotesite.timing.MultiCallRequestTimer;
import org.jherd.remotesite.timing.RequestTimer;
import org.jherd.servlet.ContentNotFoundException;
import org.jherd.servlet.RequestNs;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.uriplay.beans.NullProjector;
import org.uriplay.beans.ProjectionException;
import org.uriplay.beans.Projector;
import org.uriplay.media.entity.Description;

import com.google.soy.common.collect.Lists;

/**
 * Controller to handle the query interface to UriPlay.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author Lee Denison (lee@metabroadcast.com)
 */
@Controller
public class UriFetchingController {

	private static final String VIEW = "uriplayModel";
	
	private final Projector projector;
	private final Factory<RequestTimer> timerFactory;

	private final Fetcher<Description> queryExecutor;

	public UriFetchingController(Fetcher<Description> queryExecutor) {
		this(queryExecutor, new NullProjector());
	}
	
	public UriFetchingController(Fetcher<Description> queryExecutor, Projector projector) {
		this(queryExecutor, projector, new MultiCallRequestTimer());
	}
		
	UriFetchingController(Fetcher<Description> queryExecutor, Projector projector, Factory<RequestTimer> timerFactory) {
		this.queryExecutor = queryExecutor;
		this.projector = projector;
		this.timerFactory = timerFactory;
	}

	@RequestMapping(method=RequestMethod.GET)
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, 
			                   @RequestParam(defaultValue="false") boolean outputTimingInfo) throws Exception {
		
		String uri = request.getParameter("uri");
		
		if (uri == null) {
			throw new IllegalArgumentException("No uri specified");
		}
		
		RequestTimer timer = timerFactory.create();
		timer.start(this, uri);
		timer.nest();
		Query query = new Query(uri, outputTimingInfo);
		try {
			Description found = (Description) queryExecutor.fetch(uri, timer);
			
			if (found == null) {
				throw new ContentNotFoundException("No metadata available for : " + uri);
			}

			Collection<?> beans = Lists.newArrayList(found);
			beans = projector.applyTo(beans);
			
			return new ModelAndView(VIEW, RequestNs.GRAPH, beans);
			
		} catch (NoMatchingAdapterException nmae) {
			throw new ContentNotFoundException(nmae);
		} catch (FetchException fe) {
			throw new ContentNotFoundException(fe);
		} catch (ProjectionException pe) {
			throw new ContentNotFoundException(pe);
		} finally {
			timer.unnest();
			timer.stop(this, query.getUri());
			if (query.outputTimingInfo()) {
				timer.outputTo(response);
			}
		}
	}

	static class Query {

		private final String uri;
		private boolean outputTimingInfo = true;

		Query(String uri, boolean outputTimingInfo) {
			this.uri = uri;
			this.outputTimingInfo = outputTimingInfo;
		}

		public String getUri() {
			return uri;
		}


		public boolean outputTimingInfo() {
			return outputTimingInfo;
		}

		public void setOutputTimingInfo(boolean outputTimingInfo) {
			this.outputTimingInfo = outputTimingInfo;
		}
		
	}
}
