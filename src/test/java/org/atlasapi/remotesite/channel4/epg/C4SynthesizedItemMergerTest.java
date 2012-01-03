package org.atlasapi.remotesite.channel4.epg;

import static org.atlasapi.media.entity.Publisher.C4;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;

import java.util.Set;

import junit.framework.TestCase;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Version;
import org.joda.time.DateTime;

import com.google.common.collect.Iterables;
import com.metabroadcast.common.time.DateTimeZones;

public class C4SynthesizedItemMergerTest extends TestCase {

    public void testMerge() {

        C4SynthesizedItemMerger merger = new C4SynthesizedItemMerger();
        
        Episode synthesized = makeSynthItem();
        
        Episode canonical = makeCanonicalItem();
        
        merger.merge(synthesized, canonical);
        
        Version version = Iterables.getOnlyElement(canonical.getVersions());
        Set<Broadcast> broadcasts = version.getBroadcasts();
        assertThat(broadcasts.size(), is(equalTo(2)));
        assertThat(Iterables.get(broadcasts, 0).getSourceId(), is(isOneOf("b1", "b2")));
        assertThat(Iterables.get(broadcasts, 1).getSourceId(), is(isOneOf("b1", "b2")));
        assertFalse(Iterables.get(broadcasts, 1).getSourceId().equals(Iterables.get(broadcasts, 0).getSourceId()));
        
        Encoding encoding = Iterables.getOnlyElement(version.getManifestedAs());
        Set<Location> locations = encoding.getAvailableAt();
        assertThat(locations.size(), is(equalTo(2)));
        assertThat(Iterables.get(locations, 0).getUri(), is(isOneOf("location1", "location2")));
        assertThat(Iterables.get(locations, 1).getUri(), is(isOneOf("location1", "location2")));
        assertFalse(Iterables.get(locations, 1).getUri().equals(Iterables.get(locations, 0).getUri()));
    }

    private Episode makeCanonicalItem() {
        Episode episode = new Episode("canonUri", "canonCurie", C4);
        
        Version version = new Version();
        
        Broadcast broadcast1 = new Broadcast("telly", new DateTime(100, DateTimeZones.UTC), new DateTime(200, DateTimeZones.UTC)).withId("b1");
        version.addBroadcast(broadcast1);
        
        Encoding encoding = new Encoding();
        
        Location location1 = new Location();
        location1.setUri("location1");
        
        encoding.addAvailableAt(location1);
        
        version.addManifestedAs(encoding);
        episode.addVersion(version);
        
        return episode;
    }

    private Episode makeSynthItem() {
        Episode episode = new Episode("synthUri", "synthCurie", C4);
        Version version = new Version();
        
        Broadcast broadcast1 = new Broadcast("telly", new DateTime(100, DateTimeZones.UTC), new DateTime(200, DateTimeZones.UTC)).withId("b1");
        Broadcast broadcast2 = new Broadcast("telly", new DateTime(500, DateTimeZones.UTC), new DateTime(600, DateTimeZones.UTC)).withId("b2");
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
