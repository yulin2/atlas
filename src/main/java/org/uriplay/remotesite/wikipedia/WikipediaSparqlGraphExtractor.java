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

package org.uriplay.remotesite.wikipedia;

import static org.uriplay.media.vocabulary.DBPO.FILM;
import static org.uriplay.media.vocabulary.DBPO.PERSON;
import static org.uriplay.media.vocabulary.DBPO.TELEVISION_EPISODE;
import static org.uriplay.media.vocabulary.DBPO.TELEVISION_SHOW;
import static org.uriplay.remotesite.wikipedia.WikipediaSparqlSource.CONTAINED_IN_ID;
import static org.uriplay.remotesite.wikipedia.WikipediaSparqlSource.DESCRIPTION_ID;
import static org.uriplay.remotesite.wikipedia.WikipediaSparqlSource.ITEM_ID;
import static org.uriplay.remotesite.wikipedia.WikipediaSparqlSource.SAMEAS_ID;
import static org.uriplay.remotesite.wikipedia.WikipediaSparqlSource.TITLE_ID;

import java.util.Set;

import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.DescriptionMode;
import org.jherd.beans.Representation;
import org.jherd.remotesite.FetchException;
import org.springframework.beans.MutablePropertyValues;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Episode;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Playlist;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.ResultBinding;

/**
 * {@link BeanGraphExtractor} that processes a source obtained by performing
 * Sparql queries against Dbpedia and produces an appropriate
 * {@link Representation}.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author Lee Denison (lee@metabroadcast.com)
 */
public class WikipediaSparqlGraphExtractor implements BeanGraphExtractor<WikipediaSparqlSource> {

	public Representation extractFrom(WikipediaSparqlSource source) {
		return extractFrom(source, DescriptionMode.OPEN_WORLD);
	}

	public Representation extractFrom(WikipediaSparqlSource source, DescriptionMode mode) {

		Representation representation = new Representation();
		String rootUri = source.getCanonicalWikipediaUri();

		Set<String> rootTypes = source.getRootTypes();
		for (String type : rootTypes) {
			setBeanType(type, rootUri, representation);
		}

		ResultSet rootProperties = source.getRootProperties();
		while (rootProperties.hasNext()) {
			ResultBinding resultBinding = (ResultBinding) rootProperties.next();

			extractBeanPropertiesFrom(resultBinding, rootUri, source, representation);
		}

		ResultSet childProperties = source.getChildProperties();
		if (childProperties != null) {
			Set<String> childItems = Sets.newHashSet();
			Set<String> childLists = Sets.newHashSet();
			
			ResultSet childTypeProperties = source.getChildTypeProperties();
			while (childTypeProperties.hasNext()) {
				ResultBinding resultBinding = (ResultBinding) childTypeProperties.next();
				String childUri = resultBinding.getResource(ITEM_ID).getURI();

				Class<?> beanType = extractBeanTypeFrom(resultBinding, childUri, source, representation);
				
				if (beanType != null) {
					if (Item.class.isAssignableFrom(beanType)) {
						childItems.add(childUri);
					} else if (Playlist.class.isAssignableFrom(beanType)) {
						childLists.add(childUri);
					}
				}
			}
			
			while (childProperties.hasNext()) {
				ResultBinding resultBinding = (ResultBinding) childProperties.next();
				String childUri = resultBinding.getResource(ITEM_ID).getURI();

				extractBeanPropertiesFrom(resultBinding, childUri, source, representation);
			}
			
			representation.getValues(rootUri).addPropertyValue("items", childItems);
			representation.getValues(rootUri).addPropertyValue("playlists", childLists);
		}
		
		ResultSet containedInProperties = source.getContainedInProperties();
		Set<String> containedIn = Sets.newHashSet();
		if (containedInProperties != null) {
			while (containedInProperties.hasNext()) {
				ResultBinding resultBinding = (ResultBinding) containedInProperties.next();
				String containedInUri = resultBinding.getResource(CONTAINED_IN_ID).getURI();
				containedIn.add(containedInUri);
			}
			
			representation.getValues(rootUri).addPropertyValue("containedIn", containedIn);
		}

		representation.addAliasFor(rootUri, source.getCanonicalDbpediaUri());

		return representation;
	}

	private Class<?> extractBeanTypeFrom(ResultBinding resultBinding, String uri, WikipediaSparqlSource source, Representation representation) {
		String articleType = source.determineItemType(resultBinding);
		return setBeanType(articleType, uri, representation);
	}

	private Class<?> setBeanType(String articleType, String uri, Representation representation) {
		Class<?> beanType = determineBeanType(articleType);

		if (beanType != null) {
			if (representation.getType(uri) == null) {
				representation.addUri(uri);
				representation.addType(uri, beanType);
			} else {
				throw new FetchException("Ambiguous type for resource [" + uri + "]");
			}
		}
		
		return beanType;
	}

	private void extractBeanPropertiesFrom(ResultBinding resultBinding, String uri, WikipediaSparqlSource source, Representation representation) {
		
		MutablePropertyValues mpvs = representation.getValues(uri);
		
		if (mpvs == null) {
			mpvs = new MutablePropertyValues();
		}
		
		Literal title = resultBinding.getLiteral(TITLE_ID);
		if (title != null) {
			mpvs.addPropertyValue("title", title.getValue());
		}

		Literal description = resultBinding.getLiteral(DESCRIPTION_ID);
		if (description != null) {
			mpvs.addPropertyValue("description", description.getValue());
		}
		
		Resource sameAs = resultBinding.getResource(SAMEAS_ID);
		if (sameAs != null) {
			representation.addAliasFor(uri, sameAs.getURI());
		}
		
		representation.addValues(uri, mpvs);
	}

	private Class<?> determineBeanType(String articleType) {

		if (TELEVISION_EPISODE.equals(articleType)) {
			return Episode.class;
		} else if (TELEVISION_SHOW.equals(articleType)) {
			return Brand.class;
		} else if (PERSON.equals(articleType)) {
			return Playlist.class;
		} else if (FILM.equals(articleType)) {
			return Item.class;
		}

		return null;
	}
}
