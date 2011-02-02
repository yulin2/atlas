/* Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.query.v2;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.beans.AtlasErrorSummary;
import org.atlasapi.beans.view.RequestNs;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.query.content.parser.ApplicationConfigurationIncludingQueryBuilder;
import org.atlasapi.query.content.parser.QueryStringBackedQueryBuilder;
import org.atlasapi.query.content.parser.WebProfileDefaultQueryAttributesSetter;
import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.time.DateTimeZones;

@Controller
public class QueryController {
	
	private static final Splitter URI_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();
	
	private static final String VIEW = "contentModel";

	private final KnownTypeQueryExecutor executor;
	private final ApplicationConfigurationIncludingQueryBuilder builder;
	
	private final AdapterLog log;
	
	public QueryController(KnownTypeQueryExecutor executor, ApplicationConfigurationFetcher configFetcher, AdapterLog log) {
		this.executor = executor;
		this.log = log;
		this.builder = new ApplicationConfigurationIncludingQueryBuilder(new QueryStringBackedQueryBuilder(new WebProfileDefaultQueryAttributesSetter()), configFetcher) ;
	}
	
	@RequestMapping("/3.0/discover.*")
	public ModelAndView discover(HttpServletRequest request) {
		try {
			ContentQuery filter = builder.build(request);
			return modelAndViewFor(executor.discover(filter));
		} catch (Exception e) {
			return errorViewFor(AtlasErrorSummary.forException(e));
		}
	}
	
	@RequestMapping("/3.0/content.*")
	public ModelAndView content(HttpServletRequest request) {
		try {
			ContentQuery filter = builder.build(request);

			if (!Selection.ALL.equals(filter.getSelection())) {
				throw new IllegalArgumentException("Cannot specifiy a limit or offset here");
			}
			String commaSeperatedUris = request.getParameter("uri");
			if (commaSeperatedUris == null) {
				throw new IllegalArgumentException("No uris specified");
			}
			List<String> uris = ImmutableList.copyOf(URI_SPLITTER.split(commaSeperatedUris));
			if (Iterables.isEmpty(uris)) {
				throw new IllegalArgumentException("No uris specified");
			}
			return modelAndViewFor(executor.executeUriQuery(uris, filter));
		} catch (Exception e) {
			return errorViewFor(AtlasErrorSummary.forException(e));
		}
	}
	
     private ModelAndView errorViewFor(AtlasErrorSummary ae) {
    	log.record(new AdapterLogEntry(ae.id(), Severity.ERROR, new DateTime(DateTimeZones.UTC)).withCause(ae.exception()).withSource(this.getClass()));
    	return new ModelAndView(VIEW, RequestNs.ERROR, ae);
    }
    
    private ModelAndView modelAndViewFor(Collection<?> queryResults) {
    	if (queryResults == null) {
    		return errorViewFor(AtlasErrorSummary.forException(new Exception("Query result was null")));
    	}
    	return new ModelAndView(VIEW, RequestNs.GRAPH, queryResults);
    }
}