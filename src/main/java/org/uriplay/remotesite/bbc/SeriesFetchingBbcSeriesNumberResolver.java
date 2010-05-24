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

package org.uriplay.remotesite.bbc;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.uriplay.persistence.system.RemoteSiteClient;
import org.uriplay.remotesite.http.CommonsHttpClient;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.metabroadcast.common.base.Maybe;

public class SeriesFetchingBbcSeriesNumberResolver implements BbcSeriesNumberResolver {

	private final ConcurrentMap<String, Maybe<Integer>> cache;
	
	private final RemoteSiteClient<Reader> client;

	public SeriesFetchingBbcSeriesNumberResolver(RemoteSiteClient<Reader> client) {
		this.client = client;
		cache = new MapMaker()
			.expiration(1, TimeUnit.HOURS)
			.softValues()
			.makeComputingMap(seriesNumber());
	}
	
	private Function<String, Maybe<Integer>> seriesNumber() {
		return new Function<String, Maybe<Integer>>() {
		
			@Override
			public Maybe<Integer> apply(String uri) {
				try {
					return extractSeriesNumberFrom(client.get(canonicaliseAndCheckUri(uri)));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	public SeriesFetchingBbcSeriesNumberResolver() {
		this(new CommonsHttpClient());
	}
	
	public Maybe<Integer> seriesNumberFor(String seriesUri) {
		return cache.get(seriesUri);
	}

	private Maybe<Integer> extractSeriesNumberFrom(Reader reader) throws IOException {
		String rdf = IOUtils.toString(reader);
		Pattern p = Pattern.compile("<dc:title>Series (\\d+)</dc:title>");
		Matcher matcher = p.matcher(rdf);
		if (matcher.find()) {
			return Maybe.just(Integer.valueOf(matcher.group(1)));
		}
		return Maybe.nothing();
	}

	private String canonicaliseAndCheckUri(String seriesUri) {
		if (seriesUri.endsWith("#programme")) {
			 seriesUri.replace("#programme", "");
		}
		if (!BbcProgrammeAdapter.SLASH_PROGRAMMES_URL_PATTERN.matcher(seriesUri).matches()) {
			throw new IllegalArgumentException("Uri is not a bbc series: " + seriesUri);
		}
		return seriesUri + ".rdf";
	}
}
