package org.atlasapi.remotesite.channel4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import java.util.Collections;
import java.util.Set;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Countries;
import org.atlasapi.media.entity.Country;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.channel4.C4BrandExtractor;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.core.io.ClassPathResource;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.WireFeedInput;
import com.sun.syndication.io.XmlReader;

public class C4BrandExtractorTest extends MockObjectTestCase {

	public void testExtractingABrand() throws Exception {
		
		Brand brand = new C4BrandExtractor().extract(ramsaysKitchenNighmaresFeed());
		
		assertThat(brand.getCanonicalUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares"));
		assertThat(brand.getAliases(), hasItem("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/4od"));
		assertThat(brand.getCurie(), is("c4:ramsays-kitchen-nightmares"));
		assertThat(brand.getTitle(), is("Ramsay's Kitchen Nightmares"));
		assertThat(brand.getPublisher(), is(Publisher.C4));
		assertThat(brand.getThumbnail(), is("http://www.channel4.com/assets/programmes/images/ramsays-kitchen-nightmares/ramsays-kitchen-nightmares_200x113.jpg"));
		assertThat(brand.getImage(), is("http://www.channel4.com/assets/programmes/images/ramsays-kitchen-nightmares/ramsays-kitchen-nightmares_625x352.jpg"));

		Episode firstEpisode = (Episode) Iterables.get(brand.getItems(), 0);
		
		assertThat(firstEpisode.getCanonicalUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/4od#2922045"));
		assertThat(firstEpisode.getCurie(), is("c4:ramsays-kitchen-nightmares_2922045"));
		assertThat(firstEpisode.getTitle(), is("Series 1 Episode 1"));
		assertThat(firstEpisode.getPublisher(), is(Publisher.C4));
		assertThat(firstEpisode.getSeriesNumber(), is(1));
		assertThat(firstEpisode.getEpisodeNumber(), is(1));
		assertThat(firstEpisode.getDescription(), startsWith("Gordon Ramsay visits Bonapartes in Silsden, West Yorkshire."));
		assertThat(firstEpisode.getThumbnail(), is("http://www.channel4.com/assets/programmes/images/ramsays-kitchen-nightmares/series-1/ramsays-kitchen-nightmares-s1-20090617160732_200x113.jpg"));
		assertThat(firstEpisode.getImage(), is("http://www.channel4.com/assets/programmes/images/ramsays-kitchen-nightmares/series-1/ramsays-kitchen-nightmares-s1-20090617160732_625x352.jpg"));
		
		Version firstEpisodeVersion = Iterables.get(firstEpisode.getVersions(), 0);
		assertThat(firstEpisodeVersion.getDuration(), is((48 * 60) + 55));
		assertThat(firstEpisodeVersion.getRating(), is("http://ref.atlasapi.org/ratings/simple/adult"));
		assertThat(firstEpisodeVersion.getRatingText(), is("Strong language throughout"));
		assertThat(firstEpisodeVersion.getBroadcasts(), is(Collections.<Broadcast>emptySet()));
		
		Encoding firstEpsiodeEncoding = Iterables.get(firstEpisodeVersion.getManifestedAs(), 0); 
		
		Location firstEpsiodeLocation = Iterables.get(firstEpsiodeEncoding.getAvailableAt(), 0); 
		assertThat(firstEpsiodeLocation.getUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/4od#2922045"));
		assertThat(firstEpsiodeLocation.getTransportType(), is(TransportType.LINK));
		
		Policy firstEpisodePolicy = firstEpsiodeLocation.getPolicy();
		assertThat(firstEpisodePolicy.getAvailabilityStart(), is(new LocalDate(2009, 07, 01).toDateTimeAtStartOfDay()));
		assertThat(firstEpisodePolicy.getAvailabilityEnd(), is(new LocalDate(2010, 12, 31).plusDays(1).toDateTimeAtStartOfDay()));
		assertThat(firstEpisodePolicy.getAvailableCountries(), is((Set<Country>) Sets.newHashSet(Countries.GB, Countries.IE)));
		
		Episode episodeWithABroadcast = (Episode) Iterables.get(brand.getItems(), 4);
		Version episodeWithABroadcastVersion = Iterables.get(episodeWithABroadcast.getVersions(), 0);
		Broadcast episodeWithABroadcastBroadcast = Iterables.get(episodeWithABroadcastVersion.getBroadcasts(), 0);
		assertThat(episodeWithABroadcastBroadcast.getTransmissionTime(), is(new DateTime("2009-06-10T23:05:00.000Z")));
		assertThat(episodeWithABroadcastBroadcast.getBroadcastOn(), is("http://www.channel4.com/more4"));
	}
	
	private Feed ramsaysKitchenNighmaresFeed() {
		try {
			return (Feed) new WireFeedInput().build(new XmlReader(new ClassPathResource("4od/ramsays-kitchen-nightmares.atom").getInputStream()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
