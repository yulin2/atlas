package org.atlasapi.query.v1;

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

import org.atlasapi.media.entity.Identified;
import org.atlasapi.persistence.servlet.ContentNotFoundException;
import org.atlasapi.persistence.servlet.RequestNs;
import org.atlasapi.persistence.system.Fetcher;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.NoMatchingAdapterException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;

/**
* Controller to handle the query interface to UriPlay.
*
* @author Robert Chatley (robert@metabroadcast.com)
* @author Lee Denison (lee@metabroadcast.com)
*/
@Controller
public class UriFetchingController {

	private static final String VIEW = "contentModel";
	
	private final Fetcher<Identified> localOrRemoteFetcher;
	
	public UriFetchingController(Fetcher<Identified> fetcher) {
		this.localOrRemoteFetcher = fetcher;
	}

	@RequestMapping(method=RequestMethod.GET)
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, 
			                   @RequestParam String uri, 
			                   @RequestParam(defaultValue="false") boolean outputTimingInfo) throws Exception {
		
		Query query = new Query(uri, outputTimingInfo);
		
		try {
			Identified bean = localOrRemoteFetcher.fetch(query.getUri());
			
			if (bean == null) {
				throw new ContentNotFoundException(uri);
			}
			return new ModelAndView(VIEW, RequestNs.GRAPH, Lists.newArrayList(bean));

		} catch (NoMatchingAdapterException nmae) {
			throw new ContentNotFoundException(nmae);
		} catch (FetchException fe) {
			throw new ContentNotFoundException(fe);
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
