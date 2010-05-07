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

package org.uriplay.remotesite;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Location;

public class Matchers {

	public static EncodingMatcher encodingMatcher() {
		return new EncodingMatcher();
	}
	
	public static LocationMatcher locationMatcher() {
		return new LocationMatcher();
	}
	
	public static class EncodingMatcher extends TypeSafeMatcher<Encoding> {

		private Matcher<String> dataContainerFormatMatcher;
		private Matcher<String> audioCodingMatcher;
		private Matcher<String> videoCodingMatcher;
		private Matcher<Boolean> dogMatcher;
		private Matcher<Integer> videoHorizonalSizeMatcher;
		private Matcher<Integer> videoVerticalSizeMatcher;
		private Matcher<Iterable<Location>> locationMatcher;
		private Matcher<Integer> audioChannelsMatcher;

		public EncodingMatcher withDataContainerFormat(Matcher<String> dataContainerFormatMatcher) {
			this.dataContainerFormatMatcher = dataContainerFormatMatcher;
			return this;
		}
		
		public EncodingMatcher withDOG(Matcher<Boolean> dogMatcher) {
			this.dogMatcher = dogMatcher;
			return this;
		}

		public EncodingMatcher withAudioCoding(Matcher<String> audioCodingMatcher) {
			this.audioCodingMatcher = audioCodingMatcher;
			return this;
		}
		
		public EncodingMatcher withVideoCoding(Matcher<String> videoCodingMatcher) {
			this.videoCodingMatcher = videoCodingMatcher;
			return this;
		}
		
		public EncodingMatcher withVideoHorizonalSize(Matcher<Integer> withVideoHorizonalSizeMatcher) {
			this.videoHorizonalSizeMatcher = withVideoHorizonalSizeMatcher;
			return this;
		}
		
		public EncodingMatcher withVideoVerticalSize(Matcher<Integer> videoVerticalSizeMatcher) {
			this.videoVerticalSizeMatcher = videoVerticalSizeMatcher;
			return this;
		}
		
		public EncodingMatcher withAudioChannels(Matcher<Integer> audioChannelsMatcher) {
			this.audioChannelsMatcher = audioChannelsMatcher;
			return this;
		}
		
		
		public EncodingMatcher withLocations(Matcher<Iterable<Location>> locationMatcher) {
			this.locationMatcher = locationMatcher;
			return this;
		}
		
		@Override
		public boolean matchesSafely(Encoding encoding) {
			if (dataContainerFormatMatcher != null && !dataContainerFormatMatcher.matches(encoding.getDataContainerFormat())) {
				return false;
			}
			if (audioCodingMatcher != null && !audioCodingMatcher.matches(encoding.getAudioCoding())) {
				return false;
			}
			if (videoCodingMatcher != null && !videoCodingMatcher.matches(encoding.getVideoCoding())) {
				return false;
			}
			if (videoVerticalSizeMatcher != null && !videoVerticalSizeMatcher.matches(encoding.getVideoVerticalSize())) {
				return false;
			}
			if (videoHorizonalSizeMatcher != null && !videoHorizonalSizeMatcher.matches(encoding.getVideoHorizontalSize())) {
				return false;
			}
			
			if (audioChannelsMatcher != null && !audioChannelsMatcher.matches(encoding.getAudioChannels())) {
				return false;
			}
			
			if (dogMatcher != null && !dogMatcher.matches(encoding.getHasDOG())) {
				return false;
			}
			if (locationMatcher != null && !locationMatcher.matches(encoding.getAvailableAt())) {
				return false;
			}
			return true;
		}

		@Override
		public void describeTo(Description description) {
			description.appendValue("Encoding matching");
		}
		
	}
	
	public static class LocationMatcher extends TypeSafeMatcher<Location> {

		private Matcher<TransportType> transportTypeMatcher;
		private Matcher<String> transportSubTypeMatcher;
		private Matcher<String> uriMatcher;

		@Override
		public boolean matchesSafely(Location location) {
			if (transportTypeMatcher != null && !transportTypeMatcher.matches(location.getTransportType())) {
				return false;
			}
			if (transportSubTypeMatcher != null && !transportSubTypeMatcher.matches(location.getTransportSubType())) {
				return false;
			}
			if (uriMatcher != null && !uriMatcher.matches(location.getUri())) {
				return false;
			}
			return true;
		}

		public LocationMatcher withUri(Matcher<String> uriMatcher) {
			this.uriMatcher = uriMatcher;
			return this;
		}

		public LocationMatcher withTransportType(Matcher<TransportType> transportTypeMatcher) {
			this.transportTypeMatcher = transportTypeMatcher;
			return this;
		}
		
		public LocationMatcher withTransportSubType(Matcher<String> transportSubTypeMatcher) {
			this.transportSubTypeMatcher = transportSubTypeMatcher;
			return this;
		}

		@Override
		public void describeTo(Description description) {
			description.appendValue("Location matching");
		}
	}
}
