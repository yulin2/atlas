package org.atlasapi.remotesite.bbc.ion;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Policy.Network;
import org.atlasapi.media.entity.Policy.Platform;
import org.atlasapi.remotesite.bbc.BbcLocationPolicyIds;
import org.atlasapi.remotesite.bbc.BbcProgrammeEncodingAndLocationCreator;
import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.BbcIonDeserializer;
import org.atlasapi.remotesite.bbc.ion.IonService.MediaSetsToPoliciesFunction;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChange;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChanges;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.time.SystemClock;

public class BbcIonOndemandChangeMultipleLocationsTest {

    private static final long IPLAYER_PLAYER_ID = 12;
    private static final long WEB_SERVICE_ID = 15;

    @Test
    public void testMultipleLocations() throws IOException {
        String json = IOUtils.toString(new ClassPathResource("ion-ondemand-changes-multiple-locations.json").getInputStream());

        BbcIonDeserializer<IonOndemandChanges> deserialiser = BbcIonDeserializers.deserializerForClass(IonOndemandChanges.class);
        
        IonOndemandChanges schedule = deserialiser.deserialise(json);
        
        BbcLocationPolicyIds locationPolicyIds = BbcLocationPolicyIds
                                                    .builder()
                                                    .withIPlayerPlayerId(IPLAYER_PLAYER_ID)
                                                    .withWebServiceId(WEB_SERVICE_ID)
                                                    .build();
                
        MediaSetsToPoliciesFunction mediaSetsToPoliciesFunction = 
                new MediaSetsToPoliciesFunction(locationPolicyIds);
        BbcProgrammeEncodingAndLocationCreator creator = 
                new BbcProgrammeEncodingAndLocationCreator(mediaSetsToPoliciesFunction, new SystemClock());
        
        for (IonOndemandChange change : schedule.getBlocklist()) {
            Maybe<IonService> ionService = IonService.fromString(change.getService());
            if (ionService.hasValue()) {
                Maybe<Encoding> encoding = creator.createEncoding(change);
                assertTrue(encoding.hasValue());
                if (ionService.requireValue().equals(IonService.IPLAYER_STB_UK_STREAM_AAC_CONCRETE)) {
                    // APPLE_IPHONE4_HLS
                    checkPolicy(encoding.requireValue(), Network.WIFI, Platform.IOS, null, null);
                        
                } else if (ionService.requireValue().equals(IonService.IPLAYER_UK_STREAM_AAC_RTMP_LO_CONCRETE)) {
                    // APPLE_PHONE4_IPAD_HLS_3G
                    checkPolicy(encoding.requireValue(), Network.THREE_G, Platform.IOS, null, null);
                    
                } else if (ionService.requireValue().equals(IonService.IPLAYER_UK_STREAM_AAC_RTMP_CONCRETE)) {
                    // PC
                    checkPolicy(encoding.requireValue(), null, Platform.PC, WEB_SERVICE_ID, IPLAYER_PLAYER_ID);
                }
            }
        }
    }

    private void checkPolicy(Encoding encoding, Network network, Platform platform, Long serviceId, Long playerId) {
        assertThat(encoding.getAvailableAt().size(), is(1));
        for (Location location : encoding.getAvailableAt()) {
            Policy policy = location.getPolicy();
            if (network != null) {
                assertTrue(policy.getNetwork() != null);
                assertThat(policy.getNetwork(), equalTo(network));
            } else {
                assertFalse(policy.getNetwork() != null);
            }
            assertThat(policy.getPlatform(), equalTo(platform));
            assertThat(policy.getPlayer(), equalTo(playerId));
            assertThat(policy.getService(), equalTo(serviceId));
        }
    }
}
