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

package org.uriplay.query.content.parser;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.uriplay.query.content.parser.DateTimeInQueryParser.MalformedDateTimeException;

import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.TimeMachine;

public class DateTimeInQueryParserTest extends TestCase {

	public void testTheParser() throws Exception {
		DateTime now = new DateTime();

		Clock clock = new TimeMachine(now);
		
		DateTimeInQueryParser parser = new DateTimeInQueryParser(clock);
		
		assertEquals(parser.parse("1010101").getMillis(), 1010101);

		assertEquals(parser.parse("now()"), now);

		assertEquals(parser.parse("now()+hours(1)"), now.plusHours(1));
		assertEquals(parser.parse("now() - hours(1)"), now.minusHours(1));

		assertIsMalformed(parser, "");
		assertIsMalformed(parser, "-");
		assertIsMalformed(parser, "+");
		assertIsMalformed(parser, "now()+");
		assertIsMalformed(parser, "now()+bob");
		assertIsMalformed(parser, "now()+bob(10)");
	}
	
	private void assertIsMalformed(DateTimeInQueryParser parser, String value) {
		try {
			parser.parse(value);
			fail("Expected " + MalformedDateTimeException.class.getSimpleName());
		}
		catch (MalformedDateTimeException e) {
			// expected
		}
	}
}
