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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.uriplay.beans.NullProjector;
import org.uriplay.beans.ProjectionException;
import org.uriplay.beans.Projector;
import org.uriplay.core.Factory;
import org.uriplay.media.entity.Content;
import org.uriplay.persistence.content.query.KnownTypeQueryExecutor;
import org.uriplay.persistence.servlet.ContentNotFoundException;
import org.uriplay.persistence.servlet.RequestNs;
import org.uriplay.persistence.system.RequestTimer;
import org.uriplay.remotesite.FetchException;
import org.uriplay.remotesite.NoMatchingAdapterException;
import org.uriplay.remotesite.timing.MultiCallRequestTimer;

import com.google.soy.common.base.Splitter;

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

	private final KnownTypeQueryExecutor queryExecutor;

	public UriFetchingController(KnownTypeQueryExecutor queryExecutor) {
		this(queryExecutor, new NullProjector());
	}
	
	public UriFetchingController(KnownTypeQueryExecutor queryExecutor, Projector projector) {
		this(queryExecutor, projector, new MultiCallRequestTimer());
	}
		
	UriFetchingController(KnownTypeQueryExecutor queryExecutor, Projector projector, Factory<RequestTimer> timerFactory) {
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
		
		try {
			
			Collection<Content> found = queryExecutor.executeAnyQuery(Splitter.on(',').split(uri)).values();
			
			if (found == null) {
				throw new ContentNotFoundException("No metadata available for : " + uri);
			}

			return new ModelAndView(VIEW, RequestNs.GRAPH, found);
			
		} catch (NoMatchingAdapterException nmae) {
			throw new ContentNotFoundException(nmae);
		} catch (FetchException fe) {
			throw new ContentNotFoundException(fe);
		} catch (ProjectionException pe) {
			throw new ContentNotFoundException(pe);
		}
	}
}
