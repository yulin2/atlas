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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.query.uri.canonical.Canonicaliser;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.html.HtmlNavigator;

import com.metabroadcast.common.base.Maybe;

public class HuluItemAdapter implements SiteSpecificAdapter<Episode> {
    
    public static HuluItemAdapter basicHuluItemAdapter(HuluClient client) {
        return new HuluItemAdapter(client, new HuluItemContentExtractor(), null);
    }
    
    public static HuluItemAdapter brandFetchingHuluItemAdapter(HuluClient client, SiteSpecificAdapter<Brand> brandAdapter) {
        return new HuluItemAdapter(client, new HuluItemContentExtractor(), brandAdapter);
    }

    public static final String BASE_URI = "http://www.hulu.com/watch/";
    private static final Pattern ALIAS_PATTERN = Pattern.compile("(" + BASE_URI + "\\d+)\\/?.*");
    
    private final HuluClient client;
    private final ContentExtractor<HtmlNavigator, Episode> extractor;
    
    // If you don't set the brand adapter, it won't try to hydrate them, which
    // is important for stopping everything from spiraling out of control
    private final SiteSpecificAdapter<Brand> brandAdapter;
    
    public HuluItemAdapter(HuluClient client, ContentExtractor<HtmlNavigator, Episode> extractor, SiteSpecificAdapter<Brand> brandAdapter) {
        this.client = client;
        this.extractor = extractor;
        this.brandAdapter = brandAdapter;
    }

    @Override
    public Episode fetch(String uri) {
        Maybe<HtmlNavigator> possibleNavigator = client.get(uri);
        if (possibleNavigator.hasValue()) {
            HtmlNavigator navigator = possibleNavigator.requireValue();

            Episode episode = extractor.extract(navigator);

            if (episode.getContainer() != null && episode.getContainer().getId() != null && brandAdapter != null) {
                //we can't thread this fetch, since any attempt to write 'episode' (by an external writer) will fail before it completes.
                brandAdapter.fetch(uri);
            }

            return episode;
        }
        
        return null;
    }

    @Override
    public boolean canFetch(String uri) {
        return ALIAS_PATTERN.matcher(uri).matches();
    }

    public static class HuluItemCanonicaliser implements Canonicaliser {
        @Override
        public String canonicalise(String uri) {
            Matcher matcher = ALIAS_PATTERN.matcher(uri);
            if (matcher.matches()) {
                return matcher.group(1);
            }
            return null;
        }
    }
}
