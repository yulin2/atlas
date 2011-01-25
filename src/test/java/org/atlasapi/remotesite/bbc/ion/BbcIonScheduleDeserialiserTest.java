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
        
        IonSchedule schedule = deserialiser.scheduleFrom(json);
        
        assertThat(schedule.getCount(), is(38));
        
        assertThat(schedule.getLink().getRel(), is("self"));
        
        assertThat(schedule.getBlocklist().size(), is(38));
        
        assertThat(schedule.getBlocklist().get(0).getHasGuidance(), is(false));
    }

}
