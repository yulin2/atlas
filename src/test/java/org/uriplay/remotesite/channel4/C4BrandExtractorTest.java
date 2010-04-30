package org.uriplay.remotesite.channel4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Collections;

import org.jmock.integration.junit3.MockObjectTestCase;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.core.io.ClassPathResource;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Broadcast;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Episode;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Version;

import com.google.common.collect.Iterables;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class C4BrandExtractorTest extends MockObjectTestCase {

	public void testExtractingABrand() throws Exception {
		
		Brand brand = new C4BrandExtractor().extract(ramsaysKitchenNighmaresFeed());
		
		assertThat(brand.getCanonicalUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares"));
		assertThat(brand.getAliases(), hasItem("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/4od"));
		assertThat(brand.getCurie(), is("c4:ramsays-kitchen-nightmares"));
		assertThat(brand.getTitle(), is("Ramsay's Kitchen Nightmares"));
		assertThat(brand.getPublisher(), is("channel4.com"));

		Episode firstEpisode = (Episode) Iterables.get(brand.getItems(), 0);
		
		assertThat(firstEpisode.getCanonicalUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/4od#2922045"));
		assertThat(firstEpisode.getCurie(), is("c4:ramsays-kitchen-nightmares_2922045"));
		assertThat(firstEpisode.getTitle(), is("Series 1 Episode 1"));
		assertThat(firstEpisode.getPublisher(), is("channel4.com"));
		assertThat(firstEpisode.getSeriesNumber(), is(1));
		assertThat(firstEpisode.getEpisodeNumber(), is(1));
		assertThat(firstEpisode.getDescription(), startsWith("Gordon Ramsay visits Bonapartes in Silsden, West Yorkshire."));
		assertThat(firstEpisode.getThumbnail(), is("http://www.channel4.com/assets/programmes/images/ramsays-kitchen-nightmares/series-1/ramsays-kitchen-nightmares-s1-20090617160732_200x113.jpg"));
		assertThat(firstEpisode.getImage(), is("http://www.channel4.com/assets/programmes/images/ramsays-kitchen-nightmares/series-1/ramsays-kitchen-nightmares-s1-20090617160732_625x352.jpg"));
		
		Version firstEpisodeVersion = Iterables.get(firstEpisode.getVersions(), 0);
		assertThat(firstEpisodeVersion.getDuration(), is((48 * 60) + 55));
		assertThat(firstEpisodeVersion.getRating(), is("http://uriplay.org/ratings/simple/adult"));
		assertThat(firstEpisodeVersion.getRatingText(), is("Strong language throughout"));
		assertThat(firstEpisodeVersion.getBroadcasts(), is(Collections.<Broadcast>emptySet()));
		
		Encoding firstEpsiodeEncoding = Iterables.get(firstEpisodeVersion.getManifestedAs(), 0); 
		
		Location firstEpsiodeLocation = Iterables.get(firstEpsiodeEncoding.getAvailableAt(), 0); 
		assertThat(firstEpsiodeLocation.getUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/4od#2922045"));
		assertThat(firstEpsiodeLocation.getTransportType(), is("htmlembed"));
		assertThat(firstEpsiodeLocation.getAvailabilityStart(), is(new LocalDate(2009, 07, 01).toDateTimeAtStartOfDay()));
		assertThat(firstEpsiodeLocation.getAvailabilityEnd(), is(new LocalDate(2010, 12, 31).plusDays(1).toDateTimeAtStartOfDay()));
		
		Episode episodeWithABroadcast = (Episode) Iterables.get(brand.getItems(), 4);
		Version episodeWithABroadcastVersion = Iterables.get(episodeWithABroadcast.getVersions(), 0);
		Broadcast episodeWithABroadcastBroadcast = Iterables.get(episodeWithABroadcastVersion.getBroadcasts(), 0);
		assertThat(episodeWithABroadcastBroadcast.getTransmissionTime(), is(new DateTime("2009-06-10T23:05:00.000Z")));
		assertThat(episodeWithABroadcastBroadcast.getBroadcastOn(), is("http://www.channel4.com/more4"));
	}
	
	private SyndFeed ramsaysKitchenNighmaresFeed() {
		try {
			return new SyndFeedInput().build(new XmlReader(new ClassPathResource("4od/ramsays-kitchen-nightmares.atom").getInputStream()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
