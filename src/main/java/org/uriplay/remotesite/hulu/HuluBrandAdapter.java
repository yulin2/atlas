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

package org.uriplay.remotesite.hulu;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Episode;
import org.uriplay.media.entity.Item;
import org.uriplay.persistence.system.RequestTimer;
import org.uriplay.query.uri.canonical.Canonicaliser;
import org.uriplay.remotesite.ContentExtractor;
import org.uriplay.remotesite.FetchException;
import org.uriplay.remotesite.HttpClients;
import org.uriplay.remotesite.SiteSpecificAdapter;
import org.uriplay.remotesite.html.HtmlNavigator;

import com.google.soy.common.collect.Lists;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.SimpleHttpClient;

public class HuluBrandAdapter implements SiteSpecificAdapter<Brand> {

    public static final String BASE_URI = "http://www.hulu.com/";
    private static final Pattern SUB_BRAND_PATTERN = Pattern.compile("(" + BASE_URI + ").+?\\/([a-z\\-]+).*");
    private static final Pattern ALIAS_PATTERN = Pattern.compile("(" + BASE_URI + "[a-z\\-]+).*");
    private final SimpleHttpClient httpClient;
    private final ContentExtractor<HtmlNavigator, Brand> extractor;
    private SiteSpecificAdapter<Episode> episodeAdapter;
    static final Log LOG = LogFactory.getLog(HuluBrandAdapter.class);

    public HuluBrandAdapter() {
        this(HttpClients.webserviceClient(), new HuluBrandContentExtractor());
    }

    public HuluBrandAdapter(SimpleHttpClient httpClient, ContentExtractor<HtmlNavigator, Brand> extractor) {
        this.httpClient = httpClient;
        this.extractor = extractor;
    }

    @Override
    public Brand fetch(String uri, RequestTimer timer) {
        try {
            LOG.info("Retrieving Hulu brand: " + uri + " with " + httpClient.getClass() + " : " + httpClient.toString());
            String content = httpClient.get(uri);
            HtmlNavigator navigator = new HtmlNavigator(content);

            Brand brand = extractor.extract(navigator);
            List<Item> episodes = Lists.newArrayList();

            if (episodeAdapter != null) {
                for (Item item : brand.getItems()) {
                    Episode episode = episodeAdapter.fetch(item.getCanonicalUri(), null);
                    episode.setBrand(brand);
                    episodes.add(episode);
                }
                brand.setItems(episodes);
            }

            return brand;
        } catch (HttpException e) {
            throw new FetchException("Unable to retrieve brand from Hulu brand: " + uri, e);
        }
    }

    @Override
    public boolean canFetch(String uri) {
        return Pattern.compile(BASE_URI + "[a-z\\-]+").matcher(uri).matches() && !uri.startsWith("http://www.hulu.com/browse");
    }

    public static class HuluBrandCanonicaliser implements Canonicaliser {
        @Override
        public String canonicalise(String uri) {
            if (uri.startsWith("http://www.hulu.com/watch") || uri.startsWith("http://www.hulu.com/feed")) {
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
}
