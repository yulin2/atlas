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
import static org.atlasapi.media.vocabulary.DBPO.TELEVISION_EPISODE;
import static org.atlasapi.media.vocabulary.DBPO.TELEVISION_SHOW;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.vocabulary.DBPO;
import org.atlasapi.media.vocabulary.DBPP;
import org.atlasapi.media.vocabulary.RDF;
import org.atlasapi.media.vocabulary.RDFS;
import org.atlasapi.remotesite.BaseSource;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.sparql.SparqlQuery;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * Object that wraps data fetched from IMDB ids (via dbPedia)
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class WikipediaSparqlSource extends BaseSource {

	public static final String SPARQL_SELECT_ID = "dbpedia_resource";

	public static final String ITEM_ID = "item";

	public static final String SAMEAS_ID = "sameas";
	
	public static final String TYPE_ID = "type";

	public static final String TITLE_ID = "title";
	
	public static final String DESCRIPTION_ID = "description";

	public static final String CONTAINED_IN_ID = "containedIn";
	
	private ResultSet rootProperties;

	private String articleName;
	private String canonicalArticleName;

	private ResultSet childProperties;

	private Set<String> rootTypes;

	private ResultSet childTypeProperties;
	
	private Set<String> recognisedTypes = Sets.newHashSet(TELEVISION_EPISODE, TELEVISION_SHOW, PERSON);

	private ResultSet containedInProperties;

	public WikipediaSparqlSource(String uri) {
		super(uri);
		articleName = getArticleName(getUri(), Pattern.compile("\\/.+\\/(.+)$"));
	}

	public SparqlQuery getUriQuery() {
		return SparqlQuery.selectDistinct("uri").whereObjectOf(getDbpediaUri(), "dbpp:redirect").withPrefix("dbpp", DBPP.NS);
	}

	public SparqlQuery getChildTypesOfTVShowQuery() {
		return SparqlQuery.fromString("PREFIX dbpp: <" + DBPP.NS + "> PREFIX rdf: <" + RDF.NS +"> " +
				"SELECT DISTINCT ?" + ITEM_ID + " ?" + TYPE_ID + " WHERE { " +
				"{ ?item dbpp:series <" + getCanonicalDbpediaUri() + ">. " +
				"  ?item rdf:type <" + DBPO.TELEVISION_EPISODE + ">. " +
                "  ?item rdf:type ?type. } " +
                "UNION " +
                "{ ?item dbpp:series ?brand. ?brand dbpp:redirect <" + getCanonicalDbpediaUri() + ">. " +
				"  ?item rdf:type <" + DBPO.TELEVISION_EPISODE + ">. " +
                "  ?item rdf:type ?type. } " +
        "}");
	}
	
	public SparqlQuery getChildrenOfTVShowQuery() {
		return SparqlQuery.fromString("PREFIX dbpp: <" + DBPP.NS + "> " + "PREFIX rdf: <" + RDF.NS +"> " +
				"PREFIX foaf: <" + FOAF.NS + "> PREFIX rdfs: <" + RDFS.NS +"> " +
				"SELECT DISTINCT ?" + ITEM_ID + " ?" + TITLE_ID + " ?" + DESCRIPTION_ID + " WHERE { " +
				"{ ?item dbpp:series <" + getCanonicalDbpediaUri() + ">. " +
                "  ?item foaf:name ?title. " +
                "  ?item rdfs:comment ?description. " +
                "  ?item rdf:type <" + DBPO.TELEVISION_EPISODE + ">. } " +
                "UNION " +
            	"{ ?item dbpp:series ?brand. ?brand dbpp:redirect <" + getCanonicalDbpediaUri() + ">. " +
                "  ?item foaf:name ?title. " +
                "  ?item rdfs:comment ?description. " +
                "  ?item rdf:type <" + DBPO.TELEVISION_EPISODE + ">. } " +
        "}");
	}
	
	public SparqlQuery getChildTypesOfPersonQuery() {
		String uri = getCanonicalDbpediaUri();
		return SparqlQuery.fromString("PREFIX dbpp: <" + DBPP.NS + "> PREFIX dbpo: <" + DBPO.NS + "> PREFIX rdf: <" + RDF.NS +"> " +
				"SELECT DISTINCT ?" + ITEM_ID + " ?" + TYPE_ID + " WHERE { " +
				"{ ?item dbpo:writer <" + uri + ">. ?item rdf:type ?type. } " +
				"UNION { ?item dbpo:writer ?brand. ?brand dbpp:redirect <" + uri + ">. ?item rdf:type ?type. } " +
				"UNION { ?item dbpo:executiveproducer <" + uri + ">. ?item rdf:type ?type. } " +
				"UNION { ?item dbpo:executiveproducer ?brand. ?brand dbpp:redirect <" + uri + ">. ?item rdf:type ?type. } " +
				"UNION { ?item dbpo:creator <" + uri + ">. ?item rdf:type ?type. } " +
				"UNION { ?item dbpo:creator ?brand. ?brand dbpp:redirect <" + uri + ">. ?item rdf:type ?type. } " +
				"UNION { ?item dbpo:starring <" + uri + ">. ?item rdf:type ?type. } " +
				"UNION { ?item dbpo:starring ?brand. ?brand dbpp:redirect <" + uri + ">. ?item rdf:type ?type. } " +
				"UNION { ?item dbpo:guest <" + uri + ">. ?item rdf:type ?type. } " +
				"UNION { ?item dbpo:guest ?brand. ?brand dbpp:redirect <" + uri + ">. ?item rdf:type ?type. } " +
				"UNION { ?item dbpo:director <" + uri + ">. ?item rdf:type ?type. } " +
				"UNION { ?item dbpo:director ?brand. ?brand dbpp:redirect <" + uri + ">. ?item rdf:type ?type. } " +
				"UNION { ?item dbpo:producer <" + uri + ">. ?item rdf:type ?type. } " +
				"UNION { ?item dbpo:producer ?brand. ?brand dbpp:redirect <" + uri + ">. ?item rdf:type ?type. } " +
        "}");
	}

	public SparqlQuery getChildrenOfPersonQuery() {
		String uri = getCanonicalDbpediaUri();
		return SparqlQuery.fromString("PREFIX dbpp: <" + DBPP.NS + "> " +
				"PREFIX foaf: <" + FOAF.NS + "> PREFIX rdfs: <" + RDFS.NS +"> " +
				"SELECT DISTINCT ?" + ITEM_ID + " ?" + TITLE_ID + " ?" + DESCRIPTION_ID + " WHERE { " +
				"{ ?item dbpp:writer <" + uri + ">. ?item foaf:name ?title. ?item rdfs:comment ?description. } " +
				"UNION { ?item dbpp:writer ?brand. ?brand dbpp:redirect <" + uri + ">. ?item foaf:name ?title. ?item rdfs:comment ?description. } " +
				"UNION { ?item dbpp:executiveproducer <" + uri + ">. ?item foaf:name ?title. ?item rdfs:comment ?description. } " +
				"UNION { ?item dbpp:executiveproducer ?brand. ?brand dbpp:redirect <" + uri + ">. ?item foaf:name ?title. ?item rdfs:comment ?description. } " +
				"UNION { ?item dbpp:creator <" + uri + ">. ?item foaf:name ?title. ?item rdfs:comment ?description. } " +
				"UNION { ?item dbpp:creator ?brand. ?brand dbpp:redirect <" + uri + ">. ?item foaf:name ?title. ?item rdfs:comment ?description. } " +
				"UNION { ?item dbpp:starring <" + uri + ">. ?item foaf:name ?title. ?item rdfs:comment ?description. } " +
				"UNION { ?item dbpp:starring ?brand. ?brand dbpp:redirect <" + uri + ">. ?item foaf:name ?title. ?item rdfs:comment ?description. } " +
				"UNION { ?item dbpp:guest <" + uri + ">. ?item foaf:name ?title. ?item rdfs:comment ?description. } " +
				"UNION { ?item dbpp:guest ?brand. ?brand dbpp:redirect <" + uri + ">. ?item foaf:name ?title. ?item rdfs:comment ?description. } " +
				"UNION { ?item dbpp:director <" + uri + ">. ?item foaf:name ?title. ?item rdfs:comment ?description. } " +
				"UNION { ?item dbpp:director ?brand. ?brand dbpp:redirect <" + uri + ">. ?item foaf:name ?title. ?item rdfs:comment ?description. } " +
				"UNION { ?item dbpp:producer <" + uri + ">. ?item foaf:name ?title. ?item rdfs:comment ?description. } " +
				"UNION { ?item dbpp:producer ?brand. ?brand dbpp:redirect <" + uri + ">. ?item foaf:name ?title. ?item rdfs:comment ?description. } " +
        "}");
	}
	
	public SparqlQuery getItemTypeQuery() {
		return SparqlQuery.fromString("PREFIX rdf: <" + RDF.NS +"> " +
				"SELECT DISTINCT ?" + TYPE_ID + " WHERE  { " +
                "<" + getCanonicalDbpediaUri() + "> rdf:type ?type. " +
		"}");
	}
	
	public SparqlQuery getItemQuery() {
		return SparqlQuery.fromString("PREFIX foaf: <" + FOAF.NS + "> PREFIX rdfs: <" + RDFS.NS +"> " + 
				"PREFIX owl: <" + OWL.NS + "> " +
				"SELECT DISTINCT ?" + TITLE_ID + " ?" + DESCRIPTION_ID + " ?" + SAMEAS_ID + " WHERE  { " +
                "<" + getCanonicalDbpediaUri() + "> foaf:name ?title; " +
				"rdfs:comment ?description. " +
				"FILTER ( lang(?description) = \"en\" ) " + 
				"OPTIONAL { " +
				"  { ?" + SAMEAS_ID + " owl:sameAs <" + getCanonicalDbpediaUri() + ">. } " + 
				"  UNION " + "" +
			    "  {  <" + getCanonicalDbpediaUri() + "> owl:sameAs ?" + SAMEAS_ID + ". } " +
			    "}" +
	    	"}");
	}
	
	public SparqlQuery getContainedInQuery() {

		return SparqlQuery.fromString(
		     "PREFIX dbpp: <http://dbpedia.org/property/>"
              + "SELECT DISTINCT ?containedIn WHERE  {" 
              + "{ <" + getCanonicalDbpediaUri() + "> dbpp:writer ?" + CONTAINED_IN_ID + ". }"
			  + "UNION" 
			  + "{ <" + getCanonicalDbpediaUri() + "> dbpp:executiveProducer ?" + CONTAINED_IN_ID + ". }"  
			  + "UNION" 
			  + "{ <" + getCanonicalDbpediaUri() + "> dbpp:creator ?" + CONTAINED_IN_ID + ". }"  
			  + "UNION" 
			  + "{ <" + getCanonicalDbpediaUri() + "> dbpp:starring ?" + CONTAINED_IN_ID + ". }"  
			  + "UNION" 
			  + "{ <" + getCanonicalDbpediaUri() + "> dbpp:executiveProducer ?" + CONTAINED_IN_ID + ". }" 
			  + "UNION" 
			  + "{ <" + getCanonicalDbpediaUri() + "> dbpp:guest ?" + CONTAINED_IN_ID + ". }" 
			  + "UNION" 
			  + "{ <" + getCanonicalDbpediaUri() + "> dbpp:director ?" + CONTAINED_IN_ID + ". }" 
			  + "UNION" 
			  + "{ <" + getCanonicalDbpediaUri() + "> dbpp:producer ?" + CONTAINED_IN_ID + ". }"  
			  + "UNION" 
			  + "{ <" + getCanonicalDbpediaUri() + "> dbpp:series ?" + CONTAINED_IN_ID + ". }"
			+"}");
	}
	
	String getArticleName(String uri, Pattern pattern) {
		Matcher matcher = pattern.matcher(uri);
		if (matcher.find()) {
			return matcher.group(1);
		} 
		throw new FetchException("URI did not contain a recognised article id matching pattern: " + pattern.toString() + " in "+ getUri());
	}
	
	public void setCanonicalDbpediaUri(String canonicalDbpediaUri) {
		this.canonicalArticleName = getArticleName(canonicalDbpediaUri, Pattern.compile("\\/resource\\/(.+)$"));
	}

	public void setRootProperties(ResultSet rootProperties) {
		this.rootProperties = rootProperties;
	}

	public ResultSet getRootProperties() {
		return rootProperties;
	}

	public String getCanonicalWikipediaUri() {
		if (canonicalArticleName != null) {
			return String.format("http://en.wikipedia.org/wiki/%s", canonicalArticleName);
		}
		return String.format("http://en.wikipedia.org/wiki/%s", articleName);
	}

	public String getCanonicalDbpediaUri() {
		if (canonicalArticleName != null) {
			return String.format("http://dbpedia.org/resource/%s", canonicalArticleName);
		}
		return getDbpediaUri();
	}
	
	private String getDbpediaUri() {
		return String.format("http://dbpedia.org/resource/%s", articleName);
	}

	public void setChildProperties(ResultSet childProperties) {
		this.childProperties = childProperties;
	}
	
	public ResultSet getChildProperties() {
		return childProperties;
	}
	
	public String determineItemType(Set<String> types) {
		for (String type : types) {
			 if (recognisedTypes.contains(type)) { return type; }
		}
		
		return null;
	}

	public String determineItemType(ResultBinding binding) {
		Resource typeResource = binding.getResource(TYPE_ID);
		
		if (typeResource != null) {
			if (recognisedTypes.contains(typeResource.getURI())) {
				return typeResource.getURI();
			}
		}
		
		return null;
	}

	public void setRootTypes(Set<String> rootTypes) {
		this.rootTypes = rootTypes;
	}
	
	public Set<String> getRootTypes() {
		return rootTypes;
	}

	public void setChildTypeProperties(ResultSet childTypeProperties) {
		this.childTypeProperties = childTypeProperties;
	}
	
	public ResultSet getChildTypeProperties() {
		return childTypeProperties;
	}

	public void setContainedInProperties(ResultSet containedInProperties) {
		this.containedInProperties = containedInProperties;
	}

	public ResultSet getContainedInProperties() {
		return containedInProperties;
	}
	
}
