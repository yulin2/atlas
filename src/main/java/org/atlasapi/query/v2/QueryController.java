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

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.beans.AtlasErrorSummary;
import org.atlasapi.beans.AtlasModelWriter;
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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.time.DateTimeZones;

@Controller
public class QueryController {
	
	private static final Splitter URI_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();
	
	private final KnownTypeQueryExecutor executor;
	private final ApplicationConfigurationIncludingQueryBuilder builder;
	
	private final AdapterLog log;
	private final AtlasModelWriter outputter;
	
	public QueryController(KnownTypeQueryExecutor executor, ApplicationConfigurationFetcher configFetcher, AdapterLog log, AtlasModelWriter outputter) {
		this.executor = executor;
		this.log = log;
		this.outputter = outputter;
		this.builder = new ApplicationConfigurationIncludingQueryBuilder(new QueryStringBackedQueryBuilder(new WebProfileDefaultQueryAttributesSetter()), configFetcher) ;
	}
	
	@RequestMapping("/3.0/discover.*")
	public void discover(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			ContentQuery filter = builder.build(request);
			modelAndViewFor(request, response, executor.discover(filter));
		} catch (Exception e) {
			errorViewFor(request, response, AtlasErrorSummary.forException(e));
		}
	}
	
	@RequestMapping("/3.0/content.*")
	public void content(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
			modelAndViewFor(request, response, executor.executeUriQuery(uris, filter));
		} catch (Exception e) {
			errorViewFor(request, response, AtlasErrorSummary.forException(e));
		}
	}
	
     private void errorViewFor(HttpServletRequest request, HttpServletResponse response, AtlasErrorSummary ae) throws IOException {
    	log.record(new AdapterLogEntry(ae.id(), Severity.ERROR, new DateTime(DateTimeZones.UTC)).withCause(ae.exception()).withSource(this.getClass()));
    	outputter.writeError(request, response, ae);

    }
    
    @SuppressWarnings("unchecked")
	private void modelAndViewFor(HttpServletRequest request, HttpServletResponse response, Collection<?> queryResults) throws IOException {
    	if (queryResults == null) {
    		errorViewFor(request, response, AtlasErrorSummary.forException(new Exception("Query result was null")));
    	}
    	outputter.writeTo(request, response, (Collection<Object>) queryResults);
    }
}