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

package org.uriplay.remotesite.timing;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.uriplay.core.Factory;
import org.uriplay.persistence.system.RequestTimer;

import com.google.common.collect.Maps;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.SystemClock;

/**
 * {@link RequestTimer} that supports multiple starts/stops mapped to particular objects.
 * Can be used to time various frames of a call stack.
 * 
 * When you use the timer, call start and stop in a pair, before and after carrying out the
 * processing you want to measure. Pass the current object as the parameter to start and stop.
 * The time is associated with the type of the current object, so you can report time spent
 * "below" the current object in the call stack.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class MultiCallRequestTimer implements RequestTimer, Factory<RequestTimer> {

	private int nestingLevel = 1;
	
	private static class TimedFetch {
		
		private final int nesting;

		TimedFetch(Object fetcher, String description, int nestingLevel) {
			this.fetcher = fetcher;
			this.description = description;
			nesting = nestingLevel;
		}
		
		Object fetcher;
		String description;
		
		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
		
		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}
	}
	
	public static final String REQUEST_TIME_HEADER = "X-JHerd-RequestTime";

	private final Clock clock;
	
	private Map<TimedFetch, DateTime> startTimes = Maps.newLinkedHashMap();
	private Map<TimedFetch, DateTime> stopTimes = Maps.newHashMap();

	public MultiCallRequestTimer() {
		this(new SystemClock());
	}

	MultiCallRequestTimer(Clock clock) {
		this.clock = clock;
	}

	public void outputTo(HttpServletResponse response) {
		
		int i = 1;
		for (TimedFetch fetch : startTimes.keySet()) {
			if (stopTimes.get(fetch) != null && startTimes.get(fetch) != null) {
				int elapsedTimeMs = (int) (stopTimes.get(fetch).getMillis() - startTimes.get(fetch).getMillis());
				String description = fetch.description;
				response.addHeader(REQUEST_TIME_HEADER + i++, "[" + fetch.nesting + "] " + fetch.fetcher.getClass().getSimpleName() + "=" + elapsedTimeMs + "ms " + description);
			}
		}
	}

	public void start(Object fetcher, String description) {
		startTimes.put(new TimedFetch(fetcher, description, nestingLevel), clock.now());
	}

	public void stop(Object fetcher, String description) {
		stopTimes.put(new TimedFetch(fetcher, description, nestingLevel), clock.now());
	}

	public RequestTimer create() {
		return new MultiCallRequestTimer();
	}

	public void nest() {
		nestingLevel++;
	}

	public void unnest() {
		nestingLevel--;
	}

}
