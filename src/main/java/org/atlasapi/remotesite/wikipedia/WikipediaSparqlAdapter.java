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

package org.atlasapi.remotesite.wikipedia;

import static org.atlasapi.media.vocabulary.DBPO.PERSON;
import static org.atlasapi.media.vocabulary.DBPO.TELEVISION_SHOW;

import java.util.Set;

import org.atlasapi.media.entity.Content;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.dbpedia.DbpediaSparqlEndpoint;
import org.atlasapi.remotesite.sparql.SparqlEndpoint;
import org.atlasapi.remotesite.sparql.SparqlQuery;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.ResultBinding;

/**
 * {@link SiteSpecificAdapter} for making queries to dbPedia for information from Wikipedia.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class WikipediaSparqlAdapter implements SiteSpecificAdapter<Content> {

	private final SparqlEndpoint sparqlEndpoint;
	private final ContentExtractor<WikipediaSparqlSource, Content> propertyExtractor;
	
	public WikipediaSparqlAdapter() {
		this(new DbpediaSparqlEndpoint(), new WikipediaSparqlGraphExtractor()); 
	}
	
	WikipediaSparqlAdapter(SparqlEndpoint sparqlEndpoint, ContentExtractor<WikipediaSparqlSource, Content> propertyExtractor) {
		this.sparqlEndpoint = sparqlEndpoint;
		this.propertyExtractor = propertyExtractor;
	}

	@Override
	public Content fetch(String uri) {
		
		WikipediaSparqlSource source = new WikipediaSparqlSource(uri);
		
		try {
			
			findCanonicalDbpediaUri(source); 
			
			Set<String> rootTypes = queryTypeAndGenericItemProperties(source);
			String articleType = source.determineItemType(rootTypes);
			
			if (rootTypes.isEmpty() && articleType == null) {
				throw new FetchException("No information available for: " + uri);
			}
		
			performSubsequentQuery(source, articleType);
			
			return propertyExtractor.extract(source);
		} catch (Exception e) {
			throw new FetchException("Failed to fetch: " + uri, e);
		}
	}

	private Set<String> queryTypeAndGenericItemProperties(WikipediaSparqlSource source) {
		
		// query for item properties regardless of what type the item is.
		ResultSet propertyResult = performTimedSparqlQuery(
				source.getItemTypeQuery(), "Sparql query for type of " + source.getCanonicalDbpediaUri());
		
		Set<String> rootTypes = Sets.newHashSet();
		
		while (propertyResult.hasNext()) {
			ResultBinding binding = (ResultBinding) propertyResult.next();
			Resource typeResource = binding.getResource(WikipediaSparqlSource.TYPE_ID);	
			rootTypes.add(typeResource.getURI());
		}
		
		source.setRootTypes(rootTypes);

		propertyResult = performTimedSparqlQuery(
				source.getItemQuery(), "Sparql query for properties of " + source.getCanonicalDbpediaUri());
	
		source.setRootProperties(propertyResult);
		
		propertyResult = performTimedSparqlQuery(
				source.getContainedInQuery(), "Sparql query for resources containing " + source.getCanonicalDbpediaUri());
		
		source.setContainedInProperties(propertyResult);
		
		return rootTypes;
	}

	private void findCanonicalDbpediaUri(WikipediaSparqlSource source) {
		
		SparqlQuery query = source.getUriQuery();
		
		ResultSet canonicalUriResult =
			performTimedSparqlQuery(query, "Dbpedia sparql query for canonical uri for: " + source.getUri());
		
		//TODO: it is possible that the redirected URI is not the end of the redirect chain,
		//and is therefore not 'canonical'. We should loop until there are no more redirects.
		
		if (canonicalUriResult.hasNext()) {
			ResultBinding binding = (ResultBinding) canonicalUriResult.next();
			Resource canonicalResource = binding.getResource(query.getSelectId());
			source.setCanonicalDbpediaUri(canonicalResource.getURI());
		}
		
		//TODO: the dbpedia, wikipedia, non-canonical and canonical URIs for the resource
		//should all be added as sameAs links.
	}

	private ResultSet performTimedSparqlQuery(SparqlQuery query, String message) {
		ResultSet result = sparqlEndpoint.execute(query);
		return result;
	}

	private void performSubsequentQuery(WikipediaSparqlSource source, String articleType) {
	
		ResultSet childResult;
		
		// if the item is a person, query their works
		if (PERSON.equals(articleType)) {
			childResult = performTimedSparqlQuery(source.getChildTypesOfPersonQuery(), 
					 "Child types of Person sparql query for: " + source.getCanonicalDbpediaUri());
			source.setChildTypeProperties(childResult);			

			
			childResult = performTimedSparqlQuery(source.getChildrenOfPersonQuery(), 
					"Child properties of Person sparql query for: " + source.getCanonicalDbpediaUri());
			source.setChildProperties(childResult);			
		}
		
		// if the item is a tv show, query its episodes
		if (TELEVISION_SHOW.equals(articleType)) {
			
			childResult = performTimedSparqlQuery(source.getChildTypesOfTVShowQuery(), 
					 "Child types of TV show sparql query for: " + source.getCanonicalDbpediaUri());
			source.setChildTypeProperties(childResult);			

			childResult = performTimedSparqlQuery(source.getChildrenOfTVShowQuery(), 
					 "Sparql query for all episodes of TV show: " + source.getCanonicalDbpediaUri());
			source.setChildProperties(childResult);			
		}		
	}

	public boolean canFetch(String uri) {
		return uri.contains("wikipedia.org/wiki") || uri.contains("dbpedia.org/page") || uri.contains("dbpedia.org/resource");
	}

}
