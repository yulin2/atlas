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

package org.atlasapi.remotesite.hulu;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.query.uri.canonical.Canonicaliser;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.html.HtmlNavigator;

import com.google.common.collect.Lists;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpStatusCodeException;
import com.metabroadcast.common.http.SimpleHttpClient;

public class HuluBrandAdapter {

	private static final Log LOG = LogFactory.getLog(HuluBrandAdapter.class);

	public static final String BASE_URI = "http://www.hulu.com/";
    private static final Pattern SUB_BRAND_PATTERN = Pattern.compile("(" + BASE_URI + ").+?\\/([a-z\\-]+).*");
    private static final Pattern ALIAS_PATTERN = Pattern.compile("(" + BASE_URI + "[a-z\\-]+).*");
    private final SimpleHttpClient httpClient;
    private final ContentExtractor<HtmlNavigator, Brand> extractor;
    private SiteSpecificAdapter<Episode> episodeAdapter;
	private final ContentWriter writer;

    public HuluBrandAdapter(ContentWriter writer) {
        this(HttpClients.screenScrapingClient(), new HuluBrandContentExtractor(), writer);
    }

    public HuluBrandAdapter(SimpleHttpClient httpClient, ContentExtractor<HtmlNavigator, Brand> extractor, ContentWriter writer) {
        this.httpClient = httpClient;
        this.extractor = extractor;
		this.writer = writer;
    }

    public void loadAndSave(String uri) {
    	if (!canFetch(uri)) {
    		throw new IllegalArgumentException("Cannot load hulu uri " + uri + " as it is not in the expected format");
    	}
        LOG.info("Retrieving Hulu brand: " + uri + " with " + httpClient.getClass() + " : " + httpClient.toString());
        try {
            String content = getContent(uri);
            if (content == null) {
            	return;
            }
            HtmlNavigator navigator = new HtmlNavigator(content);

            Brand brand = extractor.extract(navigator);
            List<Episode> episodes = Lists.newArrayList();

            if (episodeAdapter != null) {
                for (Episode item : brand.getContents()) {
                    try {
                        Episode episode = episodeAdapter.fetch(item.getCanonicalUri());
                        if (episode != null) {
                        	episodes.add(episode);
                        }
                    } catch (FetchException fe) {
                        LOG.warn("Failed to retrieve episode: " + item.getCanonicalUri() + " with message: " + fe.getMessage());
                    }
                }
            }
            LOG.info("Retrieved Hulu brand: " + uri + " with " + brand.getContents().size() + " episodes");
            for (Episode episode : episodes) {
            	writer.createOrUpdate(episode);
            }
            writer.createOrUpdate(brand);
        } catch (Exception e) {
            throw new FetchException("Error retrieving Brand: " + uri + " with error: " + e.getMessage(), e);
        }
    }

    private boolean canFetch(String uri) {
        return Pattern.compile(BASE_URI + "[a-z\\-]+").matcher(uri).matches() && !ignoredUrl(uri);
    }

    public static class HuluBrandCanonicaliser implements Canonicaliser {
        @Override
        public String canonicalise(String uri) {
            if (ignoredUrl(uri)) {
                return null;
            }

            Matcher matcher = SUB_BRAND_PATTERN.matcher(uri);
            if (matcher.matches()) {
                return matcher.group(1) + matcher.group(2);
            }

            matcher = ALIAS_PATTERN.matcher(uri);
            if (matcher.matches()) {
                return matcher.group(1);
            }
            return null;
        }
    }

    public void setEpisodeAdapter(SiteSpecificAdapter<Episode> episodeAdapter) {
        this.episodeAdapter = episodeAdapter;
    }

    private String getContent(String uri) {
        for (int i = 0; i < 5; i++) {
            try {
            	String content = httpClient.getContentsOf(uri);
                if (content != null) {
                    return content;
                }
            } catch (HttpStatusCodeException e) {
            	if (HttpServletResponse.SC_NOT_FOUND == e.getStatusCode()) {
            		return null;
            	}
	        	LOG.info("Error retrieving hulu brand: " + uri + " attempt " + i + " with message: " + e.getMessage());
            } catch (HttpException e) {
	        	LOG.info("Error retrieving hulu brand: " + uri + " attempt " + i + " with message: " + e.getMessage());
	        }
        }
        throw new FetchException("Unable to retrieve brand from Hulu: " + uri + " after a number of attempts");
    }
    
    private static boolean ignoredUrl(String uri) {
        return uri.startsWith("http://www.hulu.com/browse") || uri.startsWith("http://www.hulu.com/trailers") || uri.startsWith("http://www.hulu.com/movie-trailers") || uri.startsWith("http://www.hulu.com/search") || uri.startsWith("http://www.hulu.com/watch") || uri.startsWith("http://www.hulu.com/feed");
    }
}
