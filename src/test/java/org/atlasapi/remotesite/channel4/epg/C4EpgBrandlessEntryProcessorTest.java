package org.atlasapi.remotesite.channel4.epg;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.atlasapi.media.entity.Channel.CHANNEL_FOUR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import junit.framework.TestCase;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.SystemOutAdapterLog;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.google.common.collect.ImmutableList;

public class C4EpgBrandlessEntryProcessorTest extends TestCase {

    private final AdapterLog log = new SystemOutAdapterLog();
    
    private final Mockery context = new Mockery();
    private final ContentResolver resolver = context.mock(ContentResolver.class);
    private final ContentWriter writer = context.mock(ContentWriter.class);
    
    public void testProcessNewItem() {
        
        context.checking(new Expectations(){{
            allowing(resolver).findByCanonicalUri(with(any(String.class))); will(returnValue(null));
            one(writer).createOrUpdate(with(synthesizedBrand()), with(true));
        }});
        
        C4EpgBrandlessEntryProcessor processor = new C4EpgBrandlessEntryProcessor(writer, resolver, log);
        
        processor.process(buildEntry(), CHANNEL_FOUR);
        
    }
    
    private Matcher<Brand> synthesizedBrand() {
        return new TypeSafeMatcher<Brand>() {

            @Override
            public void describeTo(Description desc) {
                desc.appendText("synthesized brand");
            }

            @Override
            public boolean matchesSafely(Brand brand) {
                assertThat(brand.getCanonicalUri(), is(equalTo("http://www.channel4.com/programmes/synthesized/robin-williams-weapons-of-self")));
                assertThat(brand.getCurie(), is(equalTo("c4:robin-williams-weapons-of-self")));
                
                assertThat(brand.getSeries().size(), is(equalTo(0)));

                ImmutableList<Episode> contents = brand.getContents();
                assertThat(contents.size(), is(equalTo(1)));
                
                Episode episode = getOnlyElement(contents);
                assertThat(episode.getCanonicalUri(), is(equalTo("http://www.channel4.com/programmes/synthesized/robin-williams-weapons-of-self/606")));
                assertThat(episode.getTitle(), is(equalTo("Robin Williams: Weapons of Self...")));
                assertThat(episode.getEpisodeNumber(), is(equalTo(null)));
                assertThat(episode.getSeriesNumber(), is(equalTo(null)));
                
                Version version = getOnlyElement(episode.getVersions());
                assertThat(version.getDuration().longValue(), is(equalTo(Duration.standardMinutes(110).getStandardSeconds())));
                
                Broadcast broadcast = getOnlyElement(version.getBroadcasts());
                assertThat(broadcast.getId(), is(equalTo("c4:606")));
                assertThat(broadcast.getTransmissionTime(), is(equalTo(new DateTime("2011-01-08T00:05:00.000Z"))));
                assertThat(broadcast.getTransmissionEndTime(), is(equalTo(new DateTime("2011-01-08T00:05:00.000Z").plus(Duration.standardMinutes(110)))));
                
                assertThat(version.getManifestedAs().isEmpty(), is(true));
                return true;
            }
        };
    }

    private C4EpgEntry buildEntry() {
        return new C4EpgEntry("tag:int.channel4.com,2009:slot/606")
            .withLinks(ImmutableList.<String>of())
            .withTitle("Robin Williams: Weapons of Self...")
            .withUpdated(new DateTime("2011-02-03T15:43:00.855Z"))
            .withSummary("...Destruction: Academy Award-winning actor, writer and comedian Robin Williams performs stand-up material at his sold-out US tour.")
            .withTxDate(new DateTime("2011-01-08T00:05:00.000Z"))
            .withTxChannel("C4")
            .withSubtitles(true)
            .withAudioDescription(false)
            .withDuration(Duration.standardMinutes(110));
    }
}
