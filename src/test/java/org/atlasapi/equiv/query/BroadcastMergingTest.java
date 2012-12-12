package org.atlasapi.equiv.query;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.SourceStatus;
import org.atlasapi.equiv.query.MergeOnOutputQueryExecutor;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class BroadcastMergingTest {

    private final MergeOnOutputQueryExecutor executor = new MergeOnOutputQueryExecutor(Mockito.mock(KnownTypeQueryExecutor.class));
    
    @Test
    public void testBroadcastMergingNoBroadcasts() {
        Item chosenItem = new Item();
        chosenItem.setCanonicalUri("chosenItem");
        Version emptyVersion = new Version();
        chosenItem.addVersion(emptyVersion);

        Item notChosenItem = new Item();
        notChosenItem.setCanonicalUri("notChosenItem");
        Version version = new Version();
        version.addBroadcast(new Broadcast("www.bbc.co.uk/services/bbctwo", new DateTime(2012,1,1,0,0,0), new DateTime(2012,1,1,0,0,0)));
        notChosenItem.addVersion(version);
        
        executor.mergeVersions(ApplicationConfiguration.defaultConfiguration(), chosenItem, ImmutableList.of(notChosenItem));
        
        assertTrue(emptyVersion.getBroadcasts().isEmpty());
    }
    
    @Test
    public void testBroadcastMergingNonMatchingBroadcasts() {
        Item chosenItem = new Item();
        chosenItem.setCanonicalUri("chosenItem");
        Version chosenVersion = new Version();
        chosenVersion.addBroadcast(new Broadcast("www.bbc.co.uk/services/bbctwo", new DateTime(2012,1,1,0,0,0), new DateTime(2012,1,1,0,0,0)));
        chosenItem.addVersion(chosenVersion);

        Item notChosenItem = new Item();
        notChosenItem.setCanonicalUri("notChosenItem");
        Version version = new Version();
        // different broadcast channel
        version.addBroadcast(new Broadcast("www.bbc.co.uk/services/bbcone", new DateTime(2012,1,1,0,0,0), new DateTime(2012,1,1,0,0,0)));
        // different start time
        version.addBroadcast(new Broadcast("www.bbc.co.uk/services/bbctwo", new DateTime(2012,1,4,0,0,0), new DateTime(2012,1,4,0,0,0)));
        notChosenItem.addVersion(version);
        
        executor.mergeVersions(ApplicationConfiguration.defaultConfiguration(), chosenItem, ImmutableList.of(notChosenItem));
        
        assertTrue(chosenVersion.getBroadcasts().isEmpty());
    }
    
    @Test
    public void testBroadcastMergingMatchingBroadcasts() {
        Item chosenItem = new Item();
        chosenItem.setCanonicalUri("chosenItem");
        Version chosenVersion = new Version();
        Broadcast chosenBroadcast = new Broadcast("www.bbc.co.uk/services/bbctwo", new DateTime(2012,1,1,0,0,0), new DateTime(2012,1,1,0,0,0));
        chosenBroadcast.addAlias("chosenBroadcast");
        chosenBroadcast.setSubtitled(true);
        chosenVersion.addBroadcast(chosenBroadcast);
        chosenItem.addVersion(chosenVersion);

        Item notChosenItem = new Item();
        notChosenItem.setCanonicalUri("notChosenItem");
        Version version = new Version();
        Broadcast broadcast = new Broadcast("www.bbc.co.uk/services/bbctwo", new DateTime(2012,1,1,0,0,0), new DateTime(2012,1,1,0,0,0));
        broadcast.addAlias("non-chosen alias");
        broadcast.setAudioDescribed(true);
        broadcast.setHighDefinition(false);
        broadcast.setSurround(false);
        broadcast.setSubtitled(false);
        version.addBroadcast(broadcast);
        notChosenItem.addVersion(version);
        
        executor.mergeVersions(ApplicationConfiguration.defaultConfiguration(), chosenItem, ImmutableList.of(notChosenItem));
        
        // ensure that the broadcast matched, 
        // and the fields on the non-chosen broadcast 
        // are merged only when the original broadcast's fields are null
        Broadcast mergedBroadcast = Iterables.getOnlyElement(chosenVersion.getBroadcasts());
        assertTrue(mergedBroadcast.getAudioDescribed());
        assertFalse(mergedBroadcast.getHighDefinition());
        assertFalse(mergedBroadcast.getSurround());
        assertTrue(mergedBroadcast.getSubtitled());
    }
    
    @Test
    public void testBroadcastMergingMatchingBroadcastsWithPrecedence() {
        Item chosenItem = new Item();
        chosenItem.setCanonicalUri("chosenItem");
        Version chosenVersion = new Version();
        Broadcast chosenBroadcast = new Broadcast("www.bbc.co.uk/services/bbctwo", new DateTime(2012,1,1,0,0,0), new DateTime(2012,1,1,0,0,0));
        chosenBroadcast.addAlias("chosenBroadcast");
        chosenBroadcast.setSubtitled(true);
        chosenVersion.addBroadcast(chosenBroadcast);
        chosenItem.addVersion(chosenVersion);

        Item notChosenBbcItem = new Item();
        notChosenBbcItem.setCanonicalUri("notChosenItem");
        Version version = new Version();
        Broadcast broadcast = new Broadcast("www.bbc.co.uk/services/bbctwo", new DateTime(2012,1,1,0,0,0), new DateTime(2012,1,1,0,0,0));
        broadcast.addAlias("non-chosen alias");
        broadcast.setAudioDescribed(true);
        broadcast.setHighDefinition(true);
        broadcast.setSubtitled(false);
        version.addBroadcast(broadcast);
        notChosenBbcItem.addVersion(version);
        
        Item notChosenFiveItem = new Item();
        notChosenFiveItem.setCanonicalUri("notChosenItem");
        version = new Version();
        broadcast = new Broadcast("www.bbc.co.uk/services/bbctwo", new DateTime(2012,1,1,0,0,0), new DateTime(2012,1,1,0,0,0));
        broadcast.addAlias("non-chosen alias");
        broadcast.setAudioDescribed(true);
        broadcast.setHighDefinition(false);
        broadcast.setSurround(false);
        broadcast.setSubtitled(false);
        version.addBroadcast(broadcast);
        notChosenFiveItem.addVersion(version);
        
        ApplicationConfiguration appConfig = ApplicationConfiguration.defaultConfiguration().copyWithPrecedence(ImmutableList.of(Publisher.BBC, Publisher.FIVE));
        
        executor.mergeVersions(appConfig, chosenItem, ImmutableList.of(notChosenBbcItem, notChosenFiveItem));
        
        // ensure that the broadcast matched, 
        // and the fields on the non-chosen broadcast 
        // are merged only when the original broadcast's fields are null
        // and that the most precedent broadcast's values are used
        Broadcast mergedBroadcast = Iterables.getOnlyElement(chosenVersion.getBroadcasts());
        assertTrue(mergedBroadcast.getAudioDescribed());
        assertTrue(mergedBroadcast.getHighDefinition());
        assertFalse(mergedBroadcast.getSurround());
        assertTrue(mergedBroadcast.getSubtitled());
    }
}
