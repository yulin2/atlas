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
import org.atlasapi.beans.SingleItemProjector;
import org.atlasapi.beans.SinglePlaylistProjector;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Playlist;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.servlet.RequestNs;
import org.atlasapi.query.content.parser.ApplicationConfigurationIncludingQueryBuilder;
import org.atlasapi.query.content.parser.QueryStringBackedQueryBuilder;
import org.atlasapi.query.content.parser.WebProfileDefaultQueryAttributesSetter;
import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.metabroadcast.common.time.DateTimeZones;

@Controller
public class QueryController {
	private static final String VIEW = "contentModel";

	private final KnownTypeQueryExecutor executor;
	private final ApplicationConfigurationIncludingQueryBuilder builder;
	
	private final SingleItemProjector itemProjector = new SingleItemProjector();
	private final SinglePlaylistProjector playlistProjector = new SinglePlaylistProjector();
	private final AdapterLog log;
	
	public QueryController(KnownTypeQueryExecutor executor, ApplicationConfigurationFetcher configFetcher, AdapterLog log) {
		this.executor = executor;
		this.log = log;
		this.builder = new ApplicationConfigurationIncludingQueryBuilder(new QueryStringBackedQueryBuilder(new WebProfileDefaultQueryAttributesSetter()), configFetcher) ;
	}
	
	@RequestMapping("/2.0/item.*")
	public ModelAndView item(HttpServletRequest request) {
		try {
			return modelAndViewFor(itemProjector.applyTo(executeItemQuery(request)));
		} catch (Exception e) {
			return errorViewFor(AtlasErrorSummary.forException(e));
		}
	}
	
	@RequestMapping("/2.0/items.*")
	public ModelAndView items(HttpServletRequest request) {
		try {
			return modelAndViewFor(executeItemQuery(request));
		} catch (Exception e) {
			return errorViewFor(AtlasErrorSummary.forException(e));
		}
	}

	@RequestMapping("/2.0/brand.*")
	public ModelAndView brand(HttpServletRequest request) {
		try {
			return modelAndViewFor(playlistProjector.applyTo(executeBrandQuery(request)));
		} catch (Exception e) {
			return errorViewFor(AtlasErrorSummary.forException(e));
		}
	}
	
	@RequestMapping("/2.0/brands.*")
	public ModelAndView brands(HttpServletRequest request) {
		try {
			return modelAndViewFor(executeBrandQuery(request));
		} catch (Exception e) {
			return errorViewFor(AtlasErrorSummary.forException(e));
		}
	}

	@RequestMapping("/2.0/playlist.*")
	public ModelAndView playlist(HttpServletRequest request) {
		try {
			return modelAndViewFor(playlistProjector.applyTo(executePlaylistQuery(request)));
		} catch (Exception e) {
			return errorViewFor(AtlasErrorSummary.forException(e));
		}
	}
	
	@RequestMapping("/2.0/playlists.*")
	public ModelAndView playlists(HttpServletRequest request) {
		try {
			return modelAndViewFor(executePlaylistQuery(request));
		} catch (Exception e) {
			return errorViewFor(AtlasErrorSummary.forException(e));
		}
	}
	
	@RequestMapping("/api/2.0/item.*")
    public ModelAndView apiItem(HttpServletRequest request) {
        return item(request);
    }
    
    @RequestMapping("/api/2.0/items.*")
    public ModelAndView apiItems(HttpServletRequest request) {
        return items(request);
    }
    

    @RequestMapping("/api/2.0/brand.*")
    public ModelAndView apiBrand(HttpServletRequest request) {
        return brand(request);
    }
    
    @RequestMapping("/api/2.0/brands.*")
    public ModelAndView apiBrands(HttpServletRequest request) {
        return brands(request);
    }
    
    @RequestMapping("/api/2.0/playlist.*")
    public ModelAndView apiPlaylist(HttpServletRequest request) {
        return playlist(request);
    }
    
    @RequestMapping("/api/2.0/playlists.*")
    public ModelAndView apiPlaylists(HttpServletRequest request) {
        return playlists(request);
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
	
	private List<Item> executeItemQuery(HttpServletRequest request) {
		return executor.executeItemQuery(build(request, Item.class));
	}

	private List<Playlist> executePlaylistQuery(HttpServletRequest request) {
		return executor.executePlaylistQuery(build(request, Playlist.class));
	}
	
	private List<Brand> executeBrandQuery(HttpServletRequest request) {
		return executor.executeBrandQuery(build(request, Brand.class));
	}

	private ContentQuery build(HttpServletRequest request, Class<? extends Content> type) {
		return builder.build(request, type);
	}
}