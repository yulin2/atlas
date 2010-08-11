package org.atlasapi.remotesite.channel4;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Series;
import org.atlasapi.query.content.PerPublisherCurieExpander;
import org.atlasapi.remotesite.ContentExtractor;

import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;

public class C4SeriesExtractor implements ContentExtractor<Feed, Series> {

	private static final Pattern SERIES_ID = Pattern.compile("series-(\\d+)");
	
	private final C4EpisodesExtractor episodesExtractor = new C4EpisodesExtractor();
	
	@Override
	public Series extract(Feed source) {

		String uri = seriesUriFrom(source);
		
		Series series = new Series(uri, PerPublisherCurieExpander.CurieAlgorithm.C4.compact(uri));
		
		series.withSeriesNumber(seriesNumberFrom(source));
		
		series.addAlias(source.getId());
		series.setTitle(source.getTitle());
		series.setDescription(source.getSubtitle().getValue());
		
		series.setDescription(series.getDescription());
		
		series.setItems(episodesExtractor.extract(source));
		return series;
	}
	
	private Integer seriesNumberFrom(Feed source) {
		Matcher matcher = SERIES_ID.matcher(source.getId());
		if (matcher.find()) {
			return Integer.valueOf(matcher.group(1));
		}
		return null;
	}

	private String seriesUriFrom(Feed source) {
		if (source.getAlternateLinks().isEmpty()) {
			throw new IllegalArgumentException("Cannot determine series uri");
		}
		return ((Link) source.getAlternateLinks().get(0)).getHref();
	}
}
