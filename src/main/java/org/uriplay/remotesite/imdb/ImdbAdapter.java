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

package org.uriplay.remotesite.imdb;

import org.uriplay.media.entity.Description;
import org.uriplay.persistence.system.Fetcher;
import org.uriplay.remotesite.FetchException;
import org.uriplay.remotesite.SiteSpecificAdapter;
import org.uriplay.remotesite.dbpedia.DbpediaSparqlEndpoint;
import org.uriplay.remotesite.sparql.SparqlEndpoint;
import org.uriplay.remotesite.sparql.SparqlQuery;
import org.uriplay.remotesite.wikipedia.WikipediaSparqlSource;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.ResultBinding;

/**
 * {@link SiteSpecificAdapter} for making queries to dbPedia for information from Wikipedia.
 * Queries for a dbpedia article referencing the given imdb id, and then delegates to another
 * adapter to process that uri.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class ImdbAdapter implements SiteSpecificAdapter<Description> {

	private final SparqlEndpoint sparqlEndpoint;
	private final Fetcher<Object> fetcher;
	
	public ImdbAdapter(Fetcher<Object> fetcher) {
		this(new DbpediaSparqlEndpoint(), fetcher); 
	}
	
	ImdbAdapter(SparqlEndpoint sparqlEndpoint, Fetcher<Object> fetcher) {
		this.sparqlEndpoint = sparqlEndpoint;
		this.fetcher = fetcher;
	}

	@Override
	public Description fetch(String imdbUri) {
		String dbpediaUri = null;
		try {
			ImdbSource source = new ImdbSource(null, imdbUri);
			dbpediaUri = findCanonicalDbpediaUri(source);
			if (dbpediaUri == null) {
				return null;
			}
			Description description = (Description) fetcher.fetch(dbpediaUri);
			String wikipediaUri = new WikipediaSparqlSource(dbpediaUri).getCanonicalWikipediaUri();
			description.addAlias(wikipediaUri);
			description.addAlias(dbpediaUri);
			return description;
		} catch (Exception e) {
			throw new FetchException("Failed to fetch: " + imdbUri, e);
		} 
	}

	private String findCanonicalDbpediaUri(ImdbSource source) {
		
		SparqlQuery query = source.getSparqlQuery();
		
		ResultSet result =
			performTimedSparqlQuery(query, "Querying dbpedia for articles referencing " + source.getUri());
		
		if (result.hasNext()) {
			ResultBinding binding = (ResultBinding) result.next();
			Resource resource = binding.getResource(query.getSelectId());
			return resource.getURI();
		}
		
		return null;
	}

	private ResultSet performTimedSparqlQuery(SparqlQuery query, String message) {
		ResultSet result = sparqlEndpoint.execute(query);
		return result;
	}

	public boolean canFetch(String uri) {
		return uri.contains("http://www.imdb.com/title/tt");
	}

}
