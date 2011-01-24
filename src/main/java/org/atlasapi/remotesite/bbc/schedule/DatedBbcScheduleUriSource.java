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

package org.atlasapi.remotesite.bbc.schedule;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.atlasapi.feeds.radioplayer.RadioPlayerService;
import org.atlasapi.feeds.radioplayer.RadioPlayerServices;
import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.SystemClock;

public class DatedBbcScheduleUriSource implements Iterable<String> {

	private final Clock clock;
	private List<String> uris;
	private int daysToLookAhead = 0;
    private Iterable<String> baseScheduleUris;
	
	public DatedBbcScheduleUriSource() {
		this(new SystemClock());
	}
	
	DatedBbcScheduleUriSource(Clock clock) {
		this.clock = clock;
	}

	private List<String> build() {
	    if(baseScheduleUris == null) {
	        baseScheduleUris = ImmutableSet.<String>builder()
	        .add("http://www.bbc.co.uk/bbcone/programmes/schedules/london")
	        .add("http://www.bbc.co.uk/bbctwo/programmes/schedules/england")
	        .add("http://www.bbc.co.uk/bbcthree/programmes/schedules")
	        .add("http://www.bbc.co.uk/bbcfour/programmes/schedules")
	        .addAll(Iterables.transform(RadioPlayerServices.services, new Function<RadioPlayerService, String>() {
	                @Override
	                public String apply(RadioPlayerService input) {
	                    return input.getScheduleUri();
	                }
	            }))
	        .build();
	    }
		
		DateTime now = clock.now();
		
		List<String> channelSchedules = Lists.newArrayListWithCapacity((daysToLookAhead+1)*Iterables.size(baseScheduleUris));
		
		for(int i = 0; i <= daysToLookAhead; i++) {
			DateTime scheduleDay = now.plusDays(i);
			String dayPart = scheduleDay.toString("yyyy/MM/dd");
			for (String baseUri : baseScheduleUris) {
			    channelSchedules.add(String.format("%s/%s.xml", baseUri, dayPart));
            }
		}
		return Collections.unmodifiableList(channelSchedules);
	}
	
	public Iterator<String> iterator() {
		if (uris == null) { uris = build(); }
		return Collections.unmodifiableList(uris).iterator();
	}

	public DatedBbcScheduleUriSource withLookAhead(int days) {
		this.daysToLookAhead = days;
		return this;
	}
	
	public DatedBbcScheduleUriSource withBaseScheduleUris(Iterable<String> baseScheduleUris) {
	    this.baseScheduleUris = baseScheduleUris;
        return this;
	}

}
