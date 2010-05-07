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

/* Copyright 2009 British Broadcasting Corporation

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

import static org.jherd.core.MimeType.AUDIO_MP4;
import static org.jherd.core.MimeType.AUDIO_MPEG;
import static org.jherd.core.MimeType.VIDEO_H263;
import static org.jherd.core.MimeType.VIDEO_H264;
import static org.jherd.core.MimeType.VIDEO_MP4;
import static org.jherd.core.MimeType.VIDEO_QUICKTIME;

import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Location;
import org.uriplay.remotesite.bbc.Policy;

/**
 * Represents different types of profile for devices that might
 * play media. For example a Web client might want a different type
 * of encoding from a mobile client or a set-top box.
 * 
 * Implements methods to match encodings and locations against a 
 * given profile.
 * 
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public enum Profile {

	DOWNLOAD {
		@Override
		public boolean matches(Location location) {
			return TransportType.DOWNLOAD.equals(location.getTransportType());
		}
	}, 
	EMBED {
		@Override
		public boolean matches(Location location) {
			return TransportType.EMBEDOBJECT.equals(location.getTransportType());
		}
	}, 
	IPHONE {
		
		@Override
		public boolean matches(Encoding encoding) {
			return encoding.hasDataContainerFormat(AUDIO_MP4, VIDEO_MP4, AUDIO_MPEG, VIDEO_QUICKTIME)
				&& (encoding.getVideoCoding() == null || encoding.hasVideoCoding(VIDEO_H263, VIDEO_H264))
				&& (encoding.getAudioCoding() == null || encoding.hasAudioCoding(AUDIO_MP4, AUDIO_MPEG))
			    && (encoding.getVideoBitRate() == null || encoding.getVideoBitRate() <= 2460)
			    && (encoding.getAudioBitRate() == null || encoding.getAudioBitRate() <= 160)
			    && (encoding.getVideoFrameRate() == null || encoding.getVideoFrameRate() <= 30)
			    && (encoding.getVideoHorizontalSize() == null || encoding.getVideoHorizontalSize() <= 640)
			    && (encoding.getVideoVerticalSize() == null || encoding.getVideoVerticalSize() <= 480)
			    && !encoding.getAvailableAt().isEmpty();
		}
		
		@Override
		public boolean matches(Location location) {
			return TransportType.DOWNLOAD.equals(location.getTransportType())
			       && "HTTP".equalsIgnoreCase(location.getTransportSubType());
		}
	}, 
	WEB {
		@Override
		public boolean matches(Location location) {
			return TransportType.HTMLEMBED.equals(location.getTransportType());
		}
	}, 
	WORLDWIDE {
		@Override
		public boolean matches(Location location) {
			return location.getRestrictedBy() == null || !location.getRestrictedBy().equals(Policy.SEVEN_DAYS_UK_ONLY);
		}
	},
	PLAYABLE {
		@Override
		public boolean matches(Location location) {
			return true;
		}
	},
	ALL {
		@Override
		public boolean matches(Location location) {
			return true;
		}
	};

	public abstract boolean matches(Location location);

	public boolean matches(Encoding encoding) {
		return !encoding.getAvailableAt().isEmpty();
	}
}
