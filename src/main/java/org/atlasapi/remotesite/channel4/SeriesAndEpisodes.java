package org.atlasapi.remotesite.channel4;

import java.util.List;

import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Series;

public class SeriesAndEpisodes {

	private final Series series;
	private final List<Episode> episodes;

	public SeriesAndEpisodes(Series series, List<Episode> episodes) {
		this.series = series;
		this.episodes = episodes;
	}
	
	public Series getSeries() {
		return series;
	}
	
	public List<Episode> getEpisodes() {
		return episodes;
	}
}
