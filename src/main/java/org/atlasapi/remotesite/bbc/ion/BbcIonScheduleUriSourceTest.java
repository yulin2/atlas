package org.atlasapi.remotesite.bbc.ion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Iterator;

import org.joda.time.DateTime;

import junit.framework.TestCase;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.time.DateTimeZones;

public class BbcIonScheduleUriSourceTest extends TestCase {

    public void testIterator() {
        DateTime today = new DateTime(DateTimeZones.UTC);
        String service = "bbc_radio_london";
        
        BbcIonScheduleUriSource uriSource = new BbcIonScheduleUriSource(ImmutableList.of(service)).withLookAhead(1).withLookBack(1);
        
        Iterator<String> uris = uriSource.iterator();
        
        assertThat(uris.next(), is(equalTo(String.format(BbcIonScheduleUriSource.SCHEDULE_PATTERN,service, today.minusDays(1).toString("yyyy-MM-dd")))));
        assertThat(uris.next(), is(equalTo(String.format(BbcIonScheduleUriSource.SCHEDULE_PATTERN,service, today.toString("yyyy-MM-dd")))));
        assertThat(uris.next(), is(equalTo(String.format(BbcIonScheduleUriSource.SCHEDULE_PATTERN,service, today.plusDays(1).toString("yyyy-MM-dd")))));
        assertThat(uris.hasNext(), is(false));
    }

}
