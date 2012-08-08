package org.atlasapi.remotesite.pa.persistence;

import org.atlasapi.media.channel.Channel;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import com.google.common.base.Optional;
import com.metabroadcast.common.persistence.MongoTestHelper;

public class MongoPaScheduleVersionStoreTest {

    private final MongoPaScheduleVersionStore scheduleVersionStore = new MongoPaScheduleVersionStore(MongoTestHelper.anEmptyTestDatabase());
    private final Channel channel = new Channel(Publisher.METABROADCAST, "test", "test", MediaType.AUDIO, "http://example.com");
 
    @Test
    public void testStore() {
        LocalDate scheduleDay = new LocalDate(2012, DateTimeConstants.JANUARY, 13);
        scheduleVersionStore.store(channel, scheduleDay, 5);
        
        assertEquals(Long.valueOf(5), scheduleVersionStore.get(channel, scheduleDay).get());
    }
    
    @Test
    public void testNoVersion() {
        LocalDate scheduleDay = new LocalDate(2012, DateTimeConstants.JANUARY, 14);
        assertEquals(Optional.<Long>absent(), scheduleVersionStore.get(channel, scheduleDay));
    }
    
    @Test
    public void testUpdate() {
        LocalDate scheduleDay = new LocalDate(2012, DateTimeConstants.JANUARY, 15);
        scheduleVersionStore.store(channel, scheduleDay, 5);
        scheduleVersionStore.store(channel, scheduleDay, 6);
        
        assertEquals(Long.valueOf(6), scheduleVersionStore.get(channel, scheduleDay).get());
    }
}
