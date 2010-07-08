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

import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.SystemClock;

public class DatedBbcScheduleUriSource implements Iterable<String> {

	private final Clock clock;
	private List<String> uris;
	private int daysToLookAhead = 0;
	
	public DatedBbcScheduleUriSource() {
		this(new SystemClock());
	}
	
	DatedBbcScheduleUriSource(Clock clock) {
		this.clock = clock;
	}

	private List<String> build() {
		
		DateTime now = clock.now();
		
		List<String> channelSchedules = Lists.newArrayList();
		
		for(int i = 0; i <= daysToLookAhead; i++) {
			DateTime scheduleDay = now.plusDays(i);
			int year = scheduleDay.getYear();
			int month = scheduleDay.getMonthOfYear();
			int day = scheduleDay.getDayOfMonth();
			channelSchedules.add(String.format("http://www.bbc.co.uk/bbcone/programmes/schedules/london/%d/%02d/%02d.xml", year, month, day));
			channelSchedules.add(String.format("http://www.bbc.co.uk/bbctwo/programmes/schedules/england/%d/%02d/%02d.xml", year, month, day));
			channelSchedules.add(String.format("http://www.bbc.co.uk/bbcthree/programmes/schedules/%d/%02d/%02d.xml", year, month, day));
			channelSchedules.add(String.format("http://www.bbc.co.uk/bbcfour/programmes/schedules/%d/%02d/%02d.xml", year, month, day));
		}
		return Collections.unmodifiableList(channelSchedules );
	}
	
	public Iterator<String> iterator() {
		if (uris == null) { uris = build(); }
		return Collections.unmodifiableList(uris).iterator();
	}

	public void setDaysToLookAhead(int days) {
		this.daysToLookAhead = days;
	}

}
