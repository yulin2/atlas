package org.atlasapi.remotesite.tvblob;

import org.atlasapi.media.entity.Episode;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.DefinitiveContentWriter;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.joda.time.DateTime;
import org.joda.time.Period;

public class TVBlobDayAdapterTest extends MockObjectTestCase {
    private ContentResolver resolver = mock(ContentResolver.class);
    private DefinitiveContentWriter writer = mock(DefinitiveContentWriter.class);
    private TVBlobDayAdapter adapter = new TVBlobDayAdapter(writer, resolver);

    public void ignoreShouldRetrieveToday() throws Exception {
        checking(new Expectations() {{
            allowing(resolver).findByUri((String) with(anything())); will(returnValue(null));
            allowing(writer).createOrUpdateDefinitiveItem((Episode) with(anything()));
        }});
        
        DateTime begin = new DateTime();
        adapter.populate("http://epgadmin.tvblob.com/api/raiuno/programmes/schedules/tomorrow.json");
        DateTime end = new DateTime();
        
        Period period = new Period(begin, end);
        System.out.println("Generated the playlist in " + period.getMinutes() + " minutes and " + period.getSeconds() + " seconds.");
    }
    
    public void testCanFetch() {
        assertTrue(adapter.canPopulate("http://epgadmin.tvblob.com/api/raiuno/programmes/schedules/tomorrow.json"));
    }
}
