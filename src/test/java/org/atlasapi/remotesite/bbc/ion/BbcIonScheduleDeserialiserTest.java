package org.atlasapi.remotesite.bbc.ion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import junit.framework.TestCase;

import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.BbcIonDeserializer;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisodeDetail;
import org.atlasapi.remotesite.bbc.ion.model.IonFeed;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.reflect.TypeToken;

public class BbcIonScheduleDeserialiserTest extends TestCase {

    public void testScheduleFrom() throws IOException {
        String json =  Resources.toString(Resources.getResource("ion-schedule.json"), Charsets.UTF_8);
        
        BbcIonDeserializer<IonSchedule> deserialiser = BbcIonDeserializers.deserializerForClass(IonSchedule.class);
        
        IonSchedule schedule = deserialiser.deserialise(json);
        
        assertThat(schedule.getCount(), is(38L));
        
        assertThat(schedule.getLink().getRel(), is("self"));
        
        assertThat(schedule.getContext().isInHd(), is(false));
        
        assertThat(schedule.getBlocklist().size(), is(38));
        
        assertThat(schedule.getBlocklist().get(0).isHasGuidance(), is(false));
    }
    
    public void testEpisodeDetailFrom() throws IOException {
        String json =  Resources.toString(Resources.getResource("ion-episode-detail.json"), Charsets.UTF_8);
        
        BbcIonDeserializer<IonFeed<IonEpisodeDetail>> deserialiser = BbcIonDeserializers.deserializerForType(new TypeToken<IonFeed<IonEpisodeDetail>>(){});
       
        IonFeed<IonEpisodeDetail> schedule = deserialiser.deserialise(json);
        
        assertThat(schedule.getCount(), is(1L));
        
        assertThat(schedule.getLink().getRel(), is("self"));
        
        assertThat(schedule.getContext().isInHd(), is(false));
        
        assertThat(schedule.getBlocklist().size(), is(1));
        
    }

}
