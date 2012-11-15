package org.atlasapi.remotesite.bbc.ion;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Policy.Network;
import org.atlasapi.media.entity.Policy.Platform;
import org.atlasapi.remotesite.bbc.BbcProgrammeEncodingAndLocationCreator;
import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.BbcIonDeserializer;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChange;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChanges;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.google.common.base.Optional;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.time.SystemClock;

public class BbcIonOndemandChangeMultipleLocationsTest {

    @Test
    public void testMultipleLocations() throws IOException {
        String json = IOUtils.toString(new ClassPathResource("ion-ondemand-changes-multiple-locations.json").getInputStream());

        BbcIonDeserializer<IonOndemandChanges> deserialiser = BbcIonDeserializers.deserializerForClass(IonOndemandChanges.class);
        
        IonOndemandChanges schedule = deserialiser.deserialise(json);
        
        BbcProgrammeEncodingAndLocationCreator creator = new BbcProgrammeEncodingAndLocationCreator(new SystemClock());
        
        for (IonOndemandChange change : schedule.getBlocklist()) {
            Maybe<IonService> ionService = IonService.fromString(change.getService());
            if (ionService.hasValue()) {
                Maybe<Encoding> encoding = creator.createEncoding(change);
                assertTrue(encoding.hasValue());
                if (ionService.requireValue().equals(IonService.IPLAYER_STB_UK_STREAM_AAC_CONCRETE)) {
                    // APPLE_IPHONE4_HLS
                    checkPolicy(encoding.requireValue(), Optional.fromNullable(Network.WIFI), Platform.IOS);
                        
                } else if (ionService.requireValue().equals(IonService.IPLAYER_UK_STREAM_AAC_RTMP_LO_CONCRETE)) {
                    // APPLE_PHONE4_IPAD_HLS_3G
                    checkPolicy(encoding.requireValue(), Optional.fromNullable(Network.THREE_G), Platform.IOS);
                    
                } else if (ionService.requireValue().equals(IonService.IPLAYER_UK_STREAM_AAC_RTMP_CONCRETE)) {
                    // PC
                    checkPolicy(encoding.requireValue(), Optional.<Network>absent(), Platform.PC);
                }
            }
        }
    }

    private void checkPolicy(Encoding encoding, Optional<Network> network, Platform platform) {
        assertThat(encoding.getAvailableAt().size(), is(1));
        for (Location location : encoding.getAvailableAt()) {
            Policy policy = location.getPolicy();
            if (network.isPresent()) {
                assertTrue(policy.getNetwork().isPresent());
                assertThat(policy.getNetwork().get(), equalTo(network.get()));
            } else {
                assertFalse(policy.getNetwork().isPresent());
            }
            assertThat(policy.getPlatform(), equalTo(platform));
        }
    }
}
