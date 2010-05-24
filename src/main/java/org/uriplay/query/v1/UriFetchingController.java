package org.uriplay.query.v1;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.uriplay.beans.NullProjector;
import org.uriplay.beans.ProjectionException;
import org.uriplay.beans.Projector;
import org.uriplay.core.Factory;
import org.uriplay.media.entity.Description;
import org.uriplay.persistence.servlet.ContentNotFoundException;
import org.uriplay.persistence.servlet.RequestNs;
import org.uriplay.persistence.system.Fetcher;
import org.uriplay.persistence.system.RequestTimer;
import org.uriplay.remotesite.FetchException;
import org.uriplay.remotesite.NoMatchingAdapterException;
import org.uriplay.remotesite.timing.MultiCallRequestTimer;

import com.google.common.collect.Lists;

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
	private final Fetcher<Description> localOrRemoteFetcher;

	public UriFetchingController(Fetcher<Description> fetcher) {
		this(fetcher, new NullProjector());
	}
	
	public UriFetchingController(Fetcher<Description> fetcher, Projector projector) {
		this(fetcher, projector, new MultiCallRequestTimer());
	}
		
	UriFetchingController(Fetcher<Description> fetcher, Projector projector, Factory<RequestTimer> timerFactory) {
		this.localOrRemoteFetcher = fetcher;
		this.projector = projector;
		this.timerFactory = timerFactory;
	}

	@RequestMapping(method=RequestMethod.GET)
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, 
			                   @RequestParam String uri, 
			                   @RequestParam(defaultValue="false") boolean outputTimingInfo) throws Exception {
		
		Query query = new Query(uri, outputTimingInfo);
		
		
		RequestTimer timer = timerFactory.create();
		timer.start(this, query.getUri());
		timer.nest();
		
		try {
			Description bean = localOrRemoteFetcher.fetch(query.getUri(), timer);
			
			if (bean == null) {
				throw new ContentNotFoundException(uri);
			}
			return new ModelAndView(VIEW, RequestNs.GRAPH, projector.applyTo(Lists.newArrayList(bean)));

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

		Query(String uri, boolean outputTimingInfo) {
			this.uri = uri;
			this.outputTimingInfo = outputTimingInfo;
		}

		private String uri;
		private boolean outputTimingInfo = true;

		public boolean outputTimingInfo() {
			return outputTimingInfo;
		}

		public void setOutputTimingInfo(boolean outputTimingInfo) {
			this.outputTimingInfo = outputTimingInfo;
		}
		
		public void setUri(String uri) {
			this.uri = uri;
		}

		public String getUri() {
			return uri;
		}
	}
}
