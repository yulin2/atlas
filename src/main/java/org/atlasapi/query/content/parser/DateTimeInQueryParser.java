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

package org.atlasapi.query.content.parser;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.common.time.SystemClock;

public class DateTimeInQueryParser {

	private final static Pattern EXPRESSION = Pattern.compile("now\\(\\)(?:\\s?([+-])\\s?([a-zA-Z]+)\\((\\d+)\\)|)");
	
	private final Clock clock;

	public DateTimeInQueryParser(Clock clock) {
		this.clock = clock;
	}
	
	public DateTimeInQueryParser() {
		this(new SystemClock());
	}
	
	public DateTime parse(String value) throws MalformedDateTimeException {
		
		if (!StringUtils.isBlank(value) && StringUtils.isNumeric(value)) {
			return new DateTime(Long.valueOf(value), DateTimeZones.UTC);
		}
		
		Matcher matcher = EXPRESSION.matcher(value);
		if (matcher.matches()) {
			String operator = matcher.group(1);
			if (operator == null) {
				return clock.now();
			}
			else  {
				Duration duration = durationFrom(matcher.group(2), matcher.group(3));
				if ("+".equals(operator)) {
					return clock.now().plus(duration);
				} else {
					return clock.now().minus(duration);
				}
			}
		}
		
		throw new MalformedDateTimeException();
	}
	
	private Duration durationFrom(String unit, String value) {
		try {
			TimeUnit timeUnit = TimeUnit.valueOf(unit.toUpperCase());
			return new Duration(timeUnit.toMillis(Long.valueOf(value)));
		} catch (IllegalArgumentException e) {
			throw new MalformedDateTimeException(e);
		}
	}

	public static class MalformedDateTimeException extends IllegalArgumentException {

		private static final long serialVersionUID = 1L;
		
		public MalformedDateTimeException() {
			super("DateTime not in a recognised format");
		}

		public MalformedDateTimeException(Exception e) {
			super("DateTime not in a recognised format", e);
		}
	}
}
