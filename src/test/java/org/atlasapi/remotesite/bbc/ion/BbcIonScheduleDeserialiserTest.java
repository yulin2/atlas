package org.atlasapi.remotesite.bbc.ion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.atlasapi.remotesite.bbc.ion.model.IonSchedule;
import org.springframework.core.io.ClassPathResource;

public class BbcIonScheduleDeserialiserTest extends TestCase {

    public void testScheduleFrom() throws IOException {
        String json = IOUtils.toString(new ClassPathResource("ion-schedule.json").getInputStream());
        
        BbcIonScheduleDeserialiser deserialiser = new BbcIonScheduleDeserialiser();
        
        assertThat(deserialiser.deserialise("0", Boolean.class), is(false));
        
        IonSchedule schedule = deserialiser.deserialise(json, IonSchedule.class);
        
        assertThat(schedule.getCount(), is(38));
        
        assertThat(schedule.getLink().getRel(), is("self"));
        
        assertThat(schedule.getContext().isInHd(), is(false));
        
        assertThat(schedule.getBlocklist().size(), is(38));
        
        assertThat(schedule.getBlocklist().get(0).isHasGuidance(), is(false));
    }

}
