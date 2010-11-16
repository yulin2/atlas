package org.atlasapi.equiv.query;

import org.atlasapi.media.entity.Episode;

import com.google.common.base.Objects;

public final class SeriesAndEpisodeNumber {
	
	private int seriesNumber;
	private int episodeNumber;
	
	public SeriesAndEpisodeNumber(Episode episode) {
		this.seriesNumber = episode.getSeriesNumber();
		this.episodeNumber = episode.getEpisodeNumber();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(seriesNumber, episodeNumber);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SeriesAndEpisodeNumber) {
			SeriesAndEpisodeNumber other = (SeriesAndEpisodeNumber) obj;
			return other.seriesNumber == seriesNumber && other.episodeNumber == episodeNumber;
		}
		return false;
	}
}
