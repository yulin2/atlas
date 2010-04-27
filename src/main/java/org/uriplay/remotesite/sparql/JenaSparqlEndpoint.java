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

package org.uriplay.remotesite.sparql;


import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;

/**
 * Wrapper that uses Jena to query a sparql endpoint with a given sparql query.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class JenaSparqlEndpoint implements SparqlEndpoint {
	
	private final String uri;
	private final String defaultGraph;

	public JenaSparqlEndpoint(String uri, String defaultGraph) {
		this.uri = uri;
		this.defaultGraph = defaultGraph;
	}

	public ResultSet execute(SparqlQuery query) {
		System.out.println("Executing Sparql: " + query.toString());
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(uri, query.toString(), defaultGraph);
		try {
			return queryExecution.execSelect();
		} finally {
			queryExecution.close();
		}
	}
}
