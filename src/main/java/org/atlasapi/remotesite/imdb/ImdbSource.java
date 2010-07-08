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

package org.atlasapi.remotesite.imdb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.remotesite.BaseSource;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.sparql.SparqlQuery;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.ResultBinding;

/**
 * Object that wraps data fetched from IMDB ids (via dbPedia)
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class ImdbSource extends BaseSource {

	public static final String SPARQL_SELECT_ID = "dbpedia_resource";
	private ResultSet resultSet;

	public ImdbSource(ResultSet resultSet, String uri) {
		super(uri);
		this.resultSet = resultSet;
	}

	public String getImdbId() {
		Pattern regex = Pattern.compile("\\/tt(\\d+)(\\/.*)?$");
		Matcher matcher = regex.matcher(getUri());
		if (matcher.find()) {
			return matcher.group(1);
		} 
		throw new FetchException("URI did not contain a recognised imdb id: " + getUri());
	}

	public Resource getResource() {
		if (resultSet.hasNext()) {
			ResultBinding item = (ResultBinding) resultSet.next();
			Resource resource = item.getResource(SPARQL_SELECT_ID);
			return resource;
		}
		
		return null;
	}

	public SparqlQuery getSparqlQuery() {
		return SparqlQuery.select(SPARQL_SELECT_ID).whereSubjectOf("dbpprop:imdbId", getImdbId()).withPrefix("dbpprop", "http://dbpedia.org/property/");
	}

}
