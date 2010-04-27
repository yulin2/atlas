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

package org.uriplay.query.uri;

import junit.framework.TestCase;

import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Location;
import org.uriplay.query.uri.Profile;
import org.uriplay.remotesite.bbc.Policy;

/**
 * Unit test for {@link Profile}.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class ProfileTest extends TestCase {
	
	public void testForIphoneProfileMatchesOnlyEncodingsInGivenAudioFormats() throws Exception {
		assertTrue(Profile.IPHONE.matches(encodingIn("audio/mpeg")));
		assertTrue(Profile.IPHONE.matches(encodingIn("audio/mp4")));
		assertFalse(Profile.IPHONE.matches(encodingIn("audio/ac3")));
	}
	
	public void testForIphoneProfileMatchesOnlyEncodingsInGivenVideoFormats() throws Exception {
		assertTrue(Profile.IPHONE.matches(encodingIn("video/quicktime")));
		assertTrue(Profile.IPHONE.matches(encodingIn("video/mp4")));
		assertFalse(Profile.IPHONE.matches(encodingIn("video/x-divx")));
	}
	
	public void testForIphoneVideoCodingIfSpecifiedMustBeOneOfThese() throws Exception {
		assertTrue(Profile.IPHONE.matches(encodingIn("video/quicktime").withVideoCoding("video/H263")));
		assertTrue(Profile.IPHONE.matches(encodingIn("video/quicktime").withVideoCoding("video/H264")));
		assertFalse(Profile.IPHONE.matches(encodingIn("video/quicktime").withVideoCoding("video/x-ms-wmv")));
	}
	
	public void testForIphoneVideoBitrateIfSpecifiedMustBeLowEnough() throws Exception {
		assertTrue(Profile.IPHONE.matches(encodingIn("video/quicktime").withVideoBitRate(2000)));
		assertFalse(Profile.IPHONE.matches(encodingIn("video/quicktime").withVideoBitRate(3000)));
	}
	
	public void testForIphoneAudioBitrateIfSpecifiedMustBeLowEnough() throws Exception {
		assertTrue(Profile.IPHONE.matches(encodingIn("video/quicktime").withAudioBitRate(160)));
		assertFalse(Profile.IPHONE.matches(encodingIn("video/quicktime").withAudioBitRate(162)));
	}
	
	public void testForIphoneVideoFramerateIfSpecifiedMustBeLowEnough() throws Exception {
		assertTrue(Profile.IPHONE.matches(encodingIn("video/quicktime").withVideoFrameRate(30)));
		assertFalse(Profile.IPHONE.matches(encodingIn("video/quicktime").withVideoFrameRate(31)));
	}

	public void testForIphoneVideoHorizontalSizeIfSpecifiedMustBeLowEnough() throws Exception {
		assertTrue(Profile.IPHONE.matches(encodingIn("video/quicktime").withVideoHorizontalSize(640)));
		assertFalse(Profile.IPHONE.matches(encodingIn("video/quicktime").withVideoHorizontalSize(641)));
	}
	
	public void testForIphoneVideoVerticalSizeIfSpecifiedMustBeLowEnough() throws Exception {
		assertTrue(Profile.IPHONE.matches(encodingIn("video/quicktime").withVideoVerticalSize(480)));
		assertFalse(Profile.IPHONE.matches(encodingIn("video/quicktime").withVideoVerticalSize(500)));
	}
	
	public void testWorldwideProfileDisallowsLocationsThatAreUkOnly() throws Exception {
		assertTrue(Profile.WORLDWIDE.matches(locationWithRestriction(Policy.SEVEN_DAYS)));
		assertFalse(Profile.WORLDWIDE.matches(locationWithRestriction(Policy.SEVEN_DAYS_UK_ONLY)));
	}

	private Location locationWithRestriction(String policy) {
		Location location;
		location = new Location();
		location.setRestrictedBy(policy);
		return location;
	}

	private Encoding encodingIn(String format) {
		Encoding encoding = new Encoding();
		encoding.setDataContainerFormat(format);
		encoding.addAvailableAt(new Location());
		return encoding;
	}

}
