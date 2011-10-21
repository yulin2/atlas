package org.atlasapi.remotesite.redux;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.logging.NullAdapterLog;
import org.atlasapi.remotesite.redux.model.FullReduxProgramme;
import org.atlasapi.remotesite.redux.model.ReduxMedia;
import org.joda.time.DateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.media.MimeType;
import com.metabroadcast.common.time.DateTimeZones;

import junit.framework.TestCase;

public class FullProgrammeItemExtractorTest extends TestCase {

    public void testExtract() {

        FullProgrammeItemExtractor extractor = new FullProgrammeItemExtractor(new NullAdapterLog());
        
        FullReduxProgramme.Builder programmeBuilder = (FullReduxProgramme.Builder) FullReduxProgramme.builder()
                    .withDiskref("5662249519293501114")
                    .withCanonical("/programme/5662249519293501114")
                    .withUri("/programme/bbconehd/2011-10-11/16-00-00")
                    .withTitle("Newsround")
                    .withDescription("CBBC. Topical news magazine for children.")
                    .withDepiction("http://g.bbcredux.com/programme/5662249519293501114/download/image-640.jpg")
                    .withService("bbconehd")
                    .withDuration("900")
                    .withSubtitles(true)
                    .withSigned(false)
                    .withHd(false)
                    .withRepeat(false)
                    .withAd(false)
                    .withWhen("2011-10-11T16:00:00Z");
        
        ReduxMedia media = new ReduxMedia(MimeType.VIDEO_MP4.toString(), "aUri", "High-rate MPEG-4", "mp4");
        programmeBuilder.withMedia(ImmutableMap.of("mp4-hi", media));
        
        FullReduxProgramme programme = programmeBuilder.build();
        Item item = extractor.extract(programme);
        
        assertEquals(FullProgrammeItemExtractor.CANONICAL_URI_BASE+programme.getCanonical(), item.getCanonicalUri());
        assertEquals(FullProgrammeItemExtractor.CURIE_BASE+programme.getDiskref(), item.getCurie());
        assertEquals(programme.getTitle(), item.getTitle());
        assertEquals(programme.getDescription(), item.getDescription());
        assertEquals(programme.getDepiction(), item.getImage());
        assertEquals("http://g.bbcredux.com/programme/5662249519293501114/download/image-74.jpg", item.getThumbnail());
        
        Version version = Iterables.getOnlyElement(item.getVersions());
        assertEquals(programme.getDuration(), version.getPublishedDuration().toString());
        
        Broadcast broadcast = Iterables.getOnlyElement(version.getBroadcasts());
        assertEquals(new DateTime(2011, 10, 11, 16, 00, 00, 000,DateTimeZones.UTC), broadcast.getTransmissionTime());
        assertEquals(new DateTime(2011, 10, 11, 16, 15, 00, 000,DateTimeZones.UTC), broadcast.getTransmissionEndTime());
        assertEquals(programme.getDuration(), broadcast.getBroadcastDuration().toString());
        assertEquals(Channel.BBC_ONE_HD.uri(), broadcast.getBroadcastOn());
        assertTrue(broadcast.getSubtitled());
        assertFalse(broadcast.getSigned());
        assertFalse(broadcast.getHighDefinition());
        assertFalse(broadcast.getRepeat());
        assertFalse(broadcast.getAudioDescribed());
        
        Encoding encoding = Iterables.getOnlyElement(version.getManifestedAs());
        assertEquals(MimeType.VIDEO_MP4, encoding.getVideoCoding());
        
        Location location = Iterables.getOnlyElement(encoding.getAvailableAt());
        
        assertEquals("http://devapi.bbcredux.com/programme/"+programme.getDiskref()+"/media/"+"mp4-hi", location.getUri());
        
    }
}
