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

package org.uriplay.query.v2;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jherd.servlet.RequestNs;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.uriplay.beans.SingleItemProjector;
import org.uriplay.beans.SinglePlaylistProjector;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Playlist;
import org.uriplay.persistence.content.query.KnownTypeQueryExecutor;
import org.uriplay.query.content.parser.QueryStringBackedQueryBuilder;

@Controller
public class QueryController {
	private static final String VIEW = "uriplayModel";

	private final KnownTypeQueryExecutor executor;
	private final QueryStringBackedQueryBuilder builder = new QueryStringBackedQueryBuilder();
	
	private final SingleItemProjector itemProjector = new SingleItemProjector();
	private final SinglePlaylistProjector playlistProjector = new SinglePlaylistProjector();
	
	public QueryController(KnownTypeQueryExecutor executor) {
		this.executor = executor;
	}
	
	@RequestMapping("/2.0/item.*")
	public ModelAndView item(HttpServletRequest request) {
		return new ModelAndView(VIEW, RequestNs.GRAPH, itemProjector.applyTo(executeItemQuery(request)));
	}
	
	@RequestMapping("/2.0/items.*")
	public ModelAndView items(HttpServletRequest request) {
		return new ModelAndView(VIEW, RequestNs.GRAPH, executeItemQuery(request));
	}
	

	@RequestMapping("/2.0/brand.*")
	public ModelAndView brand(HttpServletRequest request) {
		return new ModelAndView(VIEW, RequestNs.GRAPH, playlistProjector.applyTo(executeBrandQuery(request)));
	}
	
	@RequestMapping("/2.0/brands.*")
	public ModelAndView brands(HttpServletRequest request) {
		return new ModelAndView(VIEW, RequestNs.GRAPH, executeBrandQuery(request));
	}
	
	@RequestMapping("/2.0/playlist.*")
	public ModelAndView playlist(HttpServletRequest request) {
		return new ModelAndView(VIEW, RequestNs.GRAPH, playlistProjector.applyTo(executePlaylistQuery(request)));
	}
	
	@RequestMapping("/2.0/playlists.*")
	public ModelAndView playlists(HttpServletRequest request) {
		return new ModelAndView(VIEW, RequestNs.GRAPH, executePlaylistQuery(request));
	}
	
	private List<Item> executeItemQuery(HttpServletRequest request) {
		return executor.executeItemQuery(builder.build(request, Item.class));
	}

	private List<Playlist> executePlaylistQuery(HttpServletRequest request) {
		return executor.executePlaylistQuery(builder.build(request, Playlist.class));
	}
	
	private List<Brand> executeBrandQuery(HttpServletRequest request) {
		return executor.executeBrandQuery(builder.build(request, Brand.class));
	}
}