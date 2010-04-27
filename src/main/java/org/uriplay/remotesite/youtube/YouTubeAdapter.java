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

package org.uriplay.remotesite.youtube;

import java.util.regex.Pattern;

import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGeneratorFactory;
import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.SiteSpecificRepresentationAdapter;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.jherd.remotesite.timing.RequestTimer;
import org.jherd.remotesite.timing.TimedFetcher;

import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.util.ResourceNotFoundException;

/**
 * {@link SiteSpecificRepresentationAdapter} for querying data about video clips from YouTube.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class YouTubeAdapter extends TimedFetcher<Representation> implements SiteSpecificRepresentationAdapter {

	private static final Pattern YOUTUBE_CANONICAL_URI_PATTERN = Pattern.compile("http://www\\.youtube\\.com/watch\\?v=[^\\./&=]+");
	
	private final RemoteSiteClient<VideoEntry> gdataClient;
	private final BeanGraphExtractor<YouTubeSource> propertyExtractor;
	
	public YouTubeAdapter(IdGeneratorFactory idGeneratorFactory) {
		this(new YouTubeGDataClient(), new YouTubeGraphExtractor(idGeneratorFactory)); 
	}
	
	YouTubeAdapter(RemoteSiteClient<VideoEntry> gdataClient, BeanGraphExtractor<YouTubeSource> propertyExtractor) {
		this.gdataClient = gdataClient;
		this.propertyExtractor = propertyExtractor;
	}

	@Override
	protected Representation fetchInternal(String uri, RequestTimer timer) {
		try {
			timer.nest();
			timer.start(this, "Querying YouTube api for data about " + uri);
			VideoEntry videoEntry = gdataClient.get(uri);
			timer.stop(this, "Querying YouTube api for data about " + uri);
			timer.unnest();
			return propertyExtractor.extractFrom(new YouTubeSource(videoEntry, uri));
		} catch (ResourceNotFoundException e) {
			throw new FetchException("Video not found on YouTube: " + uri, e);
		} catch (Exception e) {
			throw new FetchException("Failed to fetch: " + uri, e);
		}
	}

	public boolean canFetch(String uri) {
		return YOUTUBE_CANONICAL_URI_PATTERN.matcher(uri).matches();
	}
}
