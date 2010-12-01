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

package org.atlasapi.query.v2;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.content.criteria.AtomicQuery;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.content.criteria.StringAttributeQuery;
import org.atlasapi.content.criteria.operator.Operators;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.servlet.ContentNotFoundException;
import org.atlasapi.persistence.servlet.RequestNs;
import org.atlasapi.query.content.CurieExpander;
import org.atlasapi.query.content.PerPublisherCurieExpander;
import org.atlasapi.query.content.UriExtractor;
import org.atlasapi.query.content.parser.ApplicationConfigurationIncludingQueryBuilder;
import org.atlasapi.query.content.parser.QueryStringBackedQueryBuilder;
import org.atlasapi.query.content.parser.WebProfileDefaultQueryAttributesSetter;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.NoMatchingAdapterException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;

/**
 * Controller to handle the query interface to UriPlay.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author Lee Denison (lee@metabroadcast.com)
 */
@Controller
public class AnyTypeFetchingController {

	private static final String VIEW = "contentModel";
	
	private final KnownTypeQueryExecutor executor;
	private final ApplicationConfigurationIncludingQueryBuilder builder;
    private final CurieExpander curieExpander = new PerPublisherCurieExpander();

	public AnyTypeFetchingController(KnownTypeQueryExecutor queryExecutor, ApplicationConfigurationFetcher configFetcher) {
		this.executor = queryExecutor;
		this.builder = new ApplicationConfigurationIncludingQueryBuilder(new QueryStringBackedQueryBuilder(new WebProfileDefaultQueryAttributesSetter()), configFetcher) ;
	}

	@RequestMapping(method=RequestMethod.GET)
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		try {
			List<Content> found = Lists.newArrayList();
			
			ContentQuery query = builder.build(request, Brand.class);
			
			found.addAll(executor.executeBrandQuery(query));
			Set<String> foundUris = foundUris(found);
			
			if (foundAllUris(foundUris, query)) {
			    return new ModelAndView(VIEW, RequestNs.GRAPH, found);
			}
			
			query = builder.build(request, Item.class);
			query = removeUris(query, foundUris);
			
			found.addAll(executor.executeItemQuery(query));
			foundUris = foundUris(found);
			
			if (foundAllUris(foundUris, query)) {
                return new ModelAndView(VIEW, RequestNs.GRAPH, found);
            }
            
            query = builder.build(request, Playlist.class);
            query = removeUris(query, foundUris);
            
			found.addAll(executor.executePlaylistQuery(query));
		
			return new ModelAndView(VIEW, RequestNs.GRAPH, found);
			
		} catch (NoMatchingAdapterException nmae) {
			throw new ContentNotFoundException(nmae);
		} catch (FetchException fe) {
			throw new ContentNotFoundException(fe);
		} 
	}
	
	private boolean foundAllUris(Set<String> foundUris, ContentQuery query) {
	    Set<String> uris = UriExtractor.extractFrom(query);
        
        if (! uris.isEmpty() && Sets.difference(uris, foundUris).isEmpty()) {
            return true;
        }
        return false;
	}
	
	private Set<String> foundUris(List<Content> contents) {
	    Set<String> uris = Sets.newHashSet();
	    for (Content content: contents) {
	        uris.add(content.getCanonicalUri());
	        uris.addAll(content.getEquivalentTo());
	    }
	    return uris;
	}
	
	@SuppressWarnings("unchecked")
    private ContentQuery removeUris(ContentQuery contentQuery, Set<String> uris) {
	    List<AtomicQuery> atomicQueries = Lists.newArrayList();
	    
	    for (AtomicQuery operand: contentQuery.operands()) {
	        if (operand instanceof StringAttributeQuery) {
	            
	            StringAttributeQuery query = (StringAttributeQuery) operand;
	            if (UriExtractor.URI_ATTRIBUTES.contains(query.getAttribute()) && query.getOperator().equals(Operators.EQUALS)) {
	                
	                Set<String> unfoundUris = Sets.newHashSet();
	                for (String value : (List<String>) query.getValue()) {
	                    Maybe<String> curieExpanded = curieExpander.expand(value);
	                    String uri = curieExpanded.hasValue() ? curieExpanded.requireValue() : value;
	                    if (! uris.contains(uri)) {
	                        unfoundUris.add(uri);
	                    }
	                }
	                
	                if (! unfoundUris.isEmpty()) {
	                    StringAttributeQuery uriQuery = new StringAttributeQuery(query.getAttribute(), query.getOperator(), unfoundUris);
	                    atomicQueries.add(uriQuery);
	                }
	            } else {
	                atomicQueries.add(operand);
	            }
	        } else {
	            atomicQueries.add(operand);
	        }
	    }
	    
	    return new ContentQuery(atomicQueries, contentQuery.getSelection(), contentQuery.getConfiguration());
	}
}
