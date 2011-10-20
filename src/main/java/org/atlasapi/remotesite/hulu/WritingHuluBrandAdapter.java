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

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.query.uri.canonical.Canonicaliser;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.html.HtmlNavigator;
import org.jdom.Element;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;

public class WritingHuluBrandAdapter implements SiteSpecificAdapter<Brand> {

	public static final String BASE_URI = "http://www.hulu.com/";
    private static final Pattern SUB_BRAND_PATTERN = Pattern.compile("(" + BASE_URI + ").+?\\/([a-z\\-]+).*");
    private static final Pattern ALIAS_PATTERN = Pattern.compile("(" + BASE_URI + "[a-z\\-]+).*");
    
    private final HuluClient client;
    private final ContentExtractor<HtmlNavigator, Brand> extractor;
    private final SiteSpecificAdapter<Episode> episodeAdapter;
	private final ContentWriter writer;
    private final AdapterLog log;
    
    public WritingHuluBrandAdapter(HuluClient client, SiteSpecificAdapter<Episode> episodeFetcher, ContentWriter writer, AdapterLog log) {
        this(client, episodeFetcher, writer, log, new HuluBaseBrandExtractor());
    }
    
    public WritingHuluBrandAdapter(HuluClient client, SiteSpecificAdapter<Episode> episodeFetcher, ContentWriter writer, AdapterLog log, ContentExtractor<HtmlNavigator, Brand> brandExtractor) {
        this.client = client;
        this.extractor = brandExtractor;
        this.episodeAdapter = episodeFetcher;
		this.writer = writer;
        this.log = log;
    }

    public Brand fetch(String uri) {
    	if (!canFetch(uri)) {
    		throw new IllegalArgumentException("Cannot load Hulu uri " + uri + " as it is not in the expected format");
    	}
        log.record(AdapterLogEntry.infoEntry().withSource(getClass()).withDescription("Retrieving Hulu brand %s",uri));
        try {
            Maybe<HtmlNavigator> possibleContent = client.get(uri);
            if (possibleContent.isNothing()) {
            	throw new FetchException("Couldn't fetch content for " + uri);
            }

            HtmlNavigator content = possibleContent.requireValue();
            Brand brand = extractor.extract(content);
            
            //We have to write here to ensure we can write the children.
            //This means that if a brand is 'fetched' on-demand it will be written twice :-/
            //Could relax referential integrity checks to avoid this.
            writer.createOrUpdate(brand);
            
            for (Element element : content.allElementsMatching("//div[@id='episode-container']/div/ul/li/a']")) {
                String href = element.getAttributeValue("href");
                String episodeUri = HuluFeed.canonicaliseEpisodeUri(href);
                if (episodeUri == null) {
                    continue;
                }
                Episode episode = episodeAdapter.fetch(episodeUri);
                if (episode != null) {
                    episode.setContainer(brand);
                    writer.createOrUpdate(episode);
                }
            }
            
            return brand;
        } catch (Exception e) {
            throw new FetchException("Error retrieving brand " + uri, e);
        }
    }

    public boolean canFetch(String uri) {
        return !ignoredUrl(uri) && Pattern.compile(BASE_URI + "[a-z\\-]+").matcher(uri).matches();
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
    
    private static boolean ignoredUrl(final String uri) {
        Set<String> ignoredPrefixes = ImmutableSet.of(
                "http://www.hulu.com/browse",
                "http://www.hulu.com/trailers",
                "http://www.hulu.com/movie-trailers",
                "http://www.hulu.com/search",
                "http://www.hulu.com/watch",
                "http://www.hulu.com/feed");

        return Iterables.any(ignoredPrefixes, new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return uri.startsWith(input);
            }
        });
    }
}
