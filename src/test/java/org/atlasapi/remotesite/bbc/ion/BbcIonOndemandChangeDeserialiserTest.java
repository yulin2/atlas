package org.atlasapi.remotesite.bbc.ion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChanges;
import org.springframework.core.io.ClassPathResource;

public class BbcIonOndemandChangeDeserialiserTest extends TestCase {

    public void testScheduleFrom() throws IOException {
        String json = IOUtils.toString(new ClassPathResource("ion-ondemand-changes.json").getInputStream());
        
        BbcIonOndemanChangeDeserialiser deserialiser = new BbcIonOndemanChangeDeserialiser();
        
        IonOndemandChanges schedule = deserialiser.deserialise(json);
        
        assertThat(schedule.getCount(), is(500));
        
        assertThat(schedule.getLink().getRel(), is("self"));
        
        assertThat(schedule.getContext().isInHd(), is(false));
        
        assertThat(schedule.getBlocklist().size(), is(500));
    }

}
