package org.atlasapi.remotesite.bbc.ion;

import org.atlasapi.media.entity.Countries;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;

import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.media.MimeType;

public enum IonService {

	IPLAYER_INTL_STREAM_MP3 {
		@Override
		public void applyTo(Encoding encoding) {
			encoding.setDataContainerFormat(MimeType.AUDIO_MP3);
			encoding.setAudioCoding(MimeType.AUDIO_MP3);
		}

		@Override
		public void applyTo(Policy policy) {
			policy.addAvailableCountry(Countries.ALL);
		}
	},
	
	IPLAYER_INTL_STREAM_AAC_WS_CONCRETE {
		@Override
		public void applyTo(Encoding encoding) {
			encoding.setDataContainerFormat(MimeType.AUDIO_AAC);
			encoding.setAudioCoding(MimeType.AUDIO_AAC);
		}
		
		@Override
		public void applyTo(Policy policy) {
			policy.addAvailableCountry(Countries.ALL);
		}
	},
	
	IPLAYER_STREAMING_H264_FLV_LO {
		
		@Override
		public void applyTo(Encoding encoding) {
			encoding.setDataContainerFormat(MimeType.APPLICATION_XSHOCKWAVEFLASH);
			encoding.setVideoCoding(MimeType.VIDEO_H264);
		}

		@Override
		public void applyTo(Policy policy) {
			policy.addAvailableCountry(Countries.GB);
		}
	},
	
	IPLAYER_UK_STREAM_AAC_RTMP_CONCRETE {

		@Override
		public void applyTo(Encoding encoding) {
			encoding.setDataContainerFormat(MimeType.AUDIO_AAC);
			encoding.setAudioCoding(MimeType.AUDIO_AAC);
		}

		@Override
		public void applyTo(Policy policy) {
			policy.addAvailableCountry(Countries.GB);
		}
	},

	IPLAYER_INTL_STREAM_AAC_RTMP_CONCRETE {

		@Override
		public void applyTo(Encoding encoding) {
			encoding.setDataContainerFormat(MimeType.AUDIO_AAC);
			encoding.setAudioCoding(MimeType.AUDIO_AAC);
		}

		@Override
		public void applyTo(Policy policy) {
			policy.addAvailableCountry(Countries.ALL);
		}
	};
	
	protected abstract void applyTo(Encoding encoding);

	protected abstract void applyTo(Policy policy);
	
	public void applyToEncoding(Encoding encoding) {
		applyTo(encoding);
		for (Location location : encoding.getAvailableAt()) {
			applyToLocation(location);
		}
	}

	public void applyToLocation(Location location) {
		Policy policy = location.getPolicy();
		if (policy == null) {
			policy = new Policy();
			location.setPolicy(policy);
		}
		applyTo(policy);
	}

	public static Maybe<IonService> fromString(String s) {
		for (IonService service : values()) {
			if (service.name().equalsIgnoreCase(s)) {
				return Maybe.just(service);
			}
		}
		return Maybe.nothing();
	}
}
