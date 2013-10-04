package org.atlasapi.remotesite.bbc.nitro;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.remotesite.bbc.nitro.extract.NitroBroadcastExtractor;
import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.base.Optional;
import com.metabroadcast.atlas.glycerin.model.BroadcastTime;
import com.metabroadcast.atlas.glycerin.model.ServiceReference;
import com.metabroadcast.common.time.DateTimeZones;


public class NitroBroadcastExtractorTest {

    private final NitroBroadcastExtractor extractor
        = new NitroBroadcastExtractor();
    
    @Test
    public void testExtractingBroadcast() throws Exception {
        
        com.metabroadcast.atlas.glycerin.model.Broadcast nitro
            = new com.metabroadcast.atlas.glycerin.model.Broadcast();
        nitro.setPid("p01g1w71");
        
        ServiceReference service = new ServiceReference();
        service.setSid("bbc_one_london");
        nitro.setService(service);
        
        XMLGregorianCalendar start = xmlGregCal("2013-09-11T05:00:00Z");
        XMLGregorianCalendar end = xmlGregCal("2013-09-11T08:15:00Z");
        BroadcastTime publishedTime = new BroadcastTime();
        publishedTime.setStart(start);
        publishedTime.setEnd(end);
        nitro.setPublishedTime(publishedTime);
        
        nitro.setIsRepeat(true);
        nitro.setIsAudioDescribed(true);
        
        Optional<Broadcast> extracted = extractor.extract(nitro);
        Broadcast atlas = extracted.get();
        
        assertThat(atlas.getBroadcastOn(), is("http://www.bbc.co.uk/services/bbcone/london"));
        assertThat(atlas.getTransmissionTime(), is(new DateTime("2013-09-11T05:00:00Z", DateTimeZones.UTC)));
        assertThat(atlas.getTransmissionEndTime(), is(new DateTime("2013-09-11T08:15:00Z", DateTimeZones.UTC)));
        assertThat(atlas.getSourceId(), is("bbc:p01g1w71"));
        assertTrue(atlas.getRepeat());
        assertTrue(atlas.getAudioDescribed());
        
    }

    private XMLGregorianCalendar xmlGregCal(String lexicalRepresentation)
            throws DatatypeConfigurationException {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(lexicalRepresentation);
    }

}
