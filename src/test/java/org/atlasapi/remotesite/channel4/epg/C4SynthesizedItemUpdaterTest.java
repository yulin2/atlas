package org.atlasapi.remotesite.channel4.epg;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.metabroadcast.common.time.DateTimeZones.UTC;
import static org.atlasapi.media.entity.Publisher.C4;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Set;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.testing.StubContentResolver;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class C4SynthesizedItemUpdaterTest extends TestCase {

    public void testFindAndUpdatePossibleSynthesized() {
        
        Mockery context = new Mockery();
        
        final ContentWriter writer = context.mock(ContentWriter.class);
        
        StubContentResolver resolver = new StubContentResolver().respondTo(synthEpisode());

        C4SynthesizedItemUpdater updater = new C4SynthesizedItemUpdater(resolver, writer);
        
        context.checking(new Expectations(){{
            one(writer).createOrUpdate(with(trimmedSynthEpisode()));
        }});
        
        Episode canonEpisode = canonEpisode();
        
        updater.findAndUpdateFromPossibleSynthesized("c4:1234", canonEpisode, "http://www.channel4.com/programmes/brand-name");
        
        context.assertIsSatisfied();
        
        Version version = getOnlyElement(canonEpisode.getVersions());
        assertThat(version.getBroadcasts().size(), is(3));
        assertThat(idsFrom(version.getBroadcasts()), is(equalTo(ImmutableSet.of("one","two","three"))));
        
        Encoding encoding = getOnlyElement(version.getManifestedAs());
        assertThat(encoding.getAvailableAt().size(), is(3));
        assertThat(urisFrom(encoding.getAvailableAt()), is(equalTo(ImmutableSet.of("location1","location2","location3"))));
    }

    private ImmutableSet<String> urisFrom(Set<Location> availableAt) {
        return ImmutableSet.copyOf(Iterables.transform(availableAt, new Function<Location, String>() {
            @Override
            public String apply(Location input) {
                return input.getUri();
            }
        }));
    }

    private ImmutableSet<String> idsFrom(Set<Broadcast> broadcasts) {
        return ImmutableSet.copyOf(Iterables.transform(broadcasts, new Function<Broadcast, String>() {
            @Override
            public String apply(Broadcast input) {
                return input.getSourceId();
            }
        }));
    }

    private Matcher<Episode> trimmedSynthEpisode() {
        return new TypeSafeMatcher<Episode>(){

            @Override
            public void describeTo(Description desc) {
                desc.appendText("synth item with no versions");
            }

            @Override
            public boolean matchesSafely(Episode synthEpisode) {
                if(!synthEpisode.getCanonicalUri().equals("http://www.channel4.com/programmes/brand-name/synthesized/1234")) {
                    return false;
                }
                return synthEpisode.getVersions().isEmpty();
            }};
    }

    private Episode synthEpisode() {
        Episode episode = new Episode("http://www.channel4.com/programmes/brand-name/synthesized/1234", "canonUri", C4);
        
        Version version = new Version();
        
        Broadcast broadcast2 = new Broadcast("telly", new DateTime(500, UTC), new DateTime(600, UTC)).withId("two");
        Broadcast broadcast3 = new Broadcast("telly", new DateTime(700, UTC), new DateTime(800, UTC)).withId("three");
        version.addBroadcast(broadcast2);
        version.addBroadcast(broadcast3);
        
        Encoding encoding = new Encoding();
        
        
        Location location2 = new Location();
        location2.setUri("location2");
        
        Location location3 = new Location();
        location3.setUri("location3");

        encoding.addAvailableAt(location2);
        encoding.addAvailableAt(location3);
        
        version.addManifestedAs(encoding);
        
        episode.addVersion(version);
        
        return episode;
    }
    
    private Episode canonEpisode() {
        Episode episode = new Episode("canonUri", "canonCurie", Publisher.C4);
        
        Version version = new Version();
        
        Broadcast broadcast1 = new Broadcast("telly", new DateTime(100, UTC), new DateTime(200, UTC)).withId("one");
        Broadcast broadcast2 = new Broadcast("telly", new DateTime(500, UTC), new DateTime(600, UTC)).withId("two");
        version.addBroadcast(broadcast1);
        version.addBroadcast(broadcast2);
        
        Encoding encoding = new Encoding();
        
        Location location1 = new Location();
        location1.setUri("location1");
        
        Location location2 = new Location();
        location2.setUri("location2");
        
        encoding.addAvailableAt(location1);
        encoding.addAvailableAt(location2);
        
        version.addManifestedAs(encoding);
        
        episode.addVersion(version);
        
        return episode;
    }

}
