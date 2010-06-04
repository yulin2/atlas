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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.uriplay.media.entity.Item;
import org.uriplay.persistence.system.RequestTimer;
import org.uriplay.query.uri.canonical.Canonicaliser;
import org.uriplay.remotesite.FetchException;
import org.uriplay.remotesite.SiteSpecificAdapter;
import org.uriplay.remotesite.html.HtmlNavigator;

import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpClientBuilder;

public class HuluAdapter implements SiteSpecificAdapter<Item> {

	public static final String BASE_URI = "http://www.hulu.com/watch/";
	private static final Pattern ALIAS_PATTERN = Pattern.compile("(" + BASE_URI + "[^/&\\?=@]+).*");
    private final SimpleHttpClient httpClient;
    private final HuluContentExtractor extractor;
	
	public HuluAdapter() {
	    this(new SimpleHttpClientBuilder().build(), new HuluContentExtractor());
	}

	public HuluAdapter(SimpleHttpClient httpClient, HuluContentExtractor extractor) {
        this.httpClient = httpClient;
        this.extractor = extractor;
    }
	
    @Override
    public Item fetch(String uri, RequestTimer timer) {
        try {
            String content = httpClient.get(uri);
            HtmlNavigator navigator = new HtmlNavigator(content);
            
            return extractor.extract(navigator);
        } catch (HttpException e) {
            throw new FetchException("Unable to retrieve from Hulu", e);
        }
    }
    
    @Override
    public boolean canFetch(String uri) {
        return ALIAS_PATTERN.matcher(uri).matches();
    }

    public static class HuluCanonicaliser implements Canonicaliser {
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
