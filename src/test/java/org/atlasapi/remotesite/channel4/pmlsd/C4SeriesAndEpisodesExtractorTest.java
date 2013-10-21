package org.atlasapi.remotesite.channel4.pmlsd;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Series;
import org.atlasapi.remotesite.channel4.pmlsd.C4SeriesAndEpisodesExtractor;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.io.Resources;
import com.metabroadcast.common.time.SystemClock;

public class C4SeriesAndEpisodesExtractorTest extends TestCase {

	private final AtomFeedBuilder seriesFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares-series-3.atom"));
	
	public void testParsingASeries() throws Exception {
		
		SetMultimap<Series, Episode> seriesAndEpisodes = new C4SeriesAndEpisodesExtractor(new SystemClock())
		    .extract(seriesFeed.build());
		Series series = Iterables.getOnlyElement(seriesAndEpisodes.keySet());
		
		assertThat(series.getCanonicalUri(), is("http://pmlsc.channel4.com/pmlsd/ramsays-kitchen-nightmares/episode-guide/series-3"));
		// TODO new alias
		assertThat(series.getAliasUrls(), is((Set<String>) ImmutableSet.of("tag:pmlsc.channel4.com,2009:/programmes/ramsays-kitchen-nightmares/episode-guide/series-3")));

		assertThat(series.getImage(), is("http://www.channel4.com/assets/programmes/images/ramsays-kitchen-nightmares/series-3/ramsays-kitchen-nightmares-s3-20090617160853_625x352.jpg"));
		assertThat(series.getThumbnail(), is("http://www.channel4.com/assets/programmes/images/ramsays-kitchen-nightmares/series-3/ramsays-kitchen-nightmares-s3-20090617160853_200x113.jpg"));

		assertThat(series.getTitle(), is("Series 3 - Ramsay's Kitchen Nightmares"));
		assertThat(series.getDescription(), startsWith("Multi Michelin-starred chef Gordon Ramsay"));
		
		Map<String,Episode> episodes = Maps.uniqueIndex(seriesAndEpisodes.values(), Identified.TO_URI);
		
		Episode firstEpisode = episodes.get("http://pmlsc.channel4.com/pmlsd/41337/001");
		
		assertThat(firstEpisode.getCanonicalUri(), is("http://pmlsc.channel4.com/pmlsd/41337/001"));
		assertThat(firstEpisode.getThumbnail(), is("http://www.channel4.com/assets/programmes/images/ramsays-kitchen-nightmares/series-3/ramsays-kitchen-nightmares-s3-20090617160853_200x113.jpg"));
		assertThat(firstEpisode.getImage(), is("http://www.channel4.com/assets/programmes/images/ramsays-kitchen-nightmares/series-3/ramsays-kitchen-nightmares-s3-20090617160853_625x352.jpg"));

		assertThat(series.getSeriesNumber(), is(3));
		
		assertThat(firstEpisode.getSeriesNumber(), is(3));
		assertThat(firstEpisode.getEpisodeNumber(), is(1));

		// since this is not a /4od feed there should be no On Demand entries
		assertThat(firstEpisode.getVersions().isEmpty(), is(true));

		// The outer adapter will notice that this is the same as the brand title (ignoring punctuation and spacing) and will replace it with the series and episode number
		assertThat(firstEpisode.getTitle(), is("Ramsay's Kitchen Nightmares"));
	}
}
