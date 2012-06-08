/* Copyright 2010 Meta Broadcast Ltd

 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
package org.atlasapi.remotesite.bbc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.remotesite.HttpClients;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.http.SimpleHttpClient;
import java.util.concurrent.ExecutionException;

public class SeriesFetchingBbcSeriesNumberResolver implements BbcSeriesNumberResolver {

    private final LoadingCache<String, Maybe<Integer>> cache;
    private final SimpleHttpClient client;

    public SeriesFetchingBbcSeriesNumberResolver() {
        this(HttpClients.webserviceClient());
    }

    public SeriesFetchingBbcSeriesNumberResolver(SimpleHttpClient client) {
        this.client = client;
        cache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).softValues().build(seriesNumber());
    }

    private CacheLoader<String, Maybe<Integer>> seriesNumber() {
        return new CacheLoader<String, Maybe<Integer>>() {

            @Override
            public Maybe<Integer> load(String uri) {
                try {
                    return extractSeriesNumberFrom(client.getContentsOf(canonicaliseAndCheckUri(uri)));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public Maybe<Integer> seriesNumberFor(String seriesUri) {
        try {
            return cache.get(seriesUri);
        } catch (ExecutionException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    private Maybe<Integer> extractSeriesNumberFrom(String rdf) throws IOException {
        Pattern p = Pattern.compile("<dc:title>Series (\\d+)</dc:title>");
        Matcher matcher = p.matcher(rdf);
        if (matcher.find()) {
            return Maybe.just(Integer.valueOf(matcher.group(1)));
        }
        return Maybe.nothing();
    }

    private String canonicaliseAndCheckUri(String seriesUri) {
        String pid = BbcFeeds.pidFrom(seriesUri);
        if (pid == null) {
            throw new IllegalArgumentException("Uri for series does not contain a PID: " + seriesUri);
        }
        return BbcFeeds.slashProgrammesUriForPid(pid) + ".rdf";
    }
}
