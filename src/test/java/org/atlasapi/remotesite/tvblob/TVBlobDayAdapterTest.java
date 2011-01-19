package org.atlasapi.remotesite.tvblob;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.jmock.Expectations;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.joda.time.DateTime;
import org.joda.time.Period;

public class TVBlobDayAdapterTest extends MockObjectTestCase {
	
    private ContentResolver resolver = mock(ContentResolver.class);
    private ContentWriter writer = mock(ContentWriter.class);
    private TVBlobDayAdapter adapter = new TVBlobDayAdapter(writer, resolver);

    public void testShouldRetrieveToday() throws Exception {
        checking(new Expectations() {{
            allowing(resolver).findByCanonicalUri((String) with(anything())); will(returnValue(null));
            allowing(writer).createOrUpdate((Episode) with(anything()));
            allowing(writer).createOrUpdate((Brand) with(anything()), with(true));
        }});
        
        DateTime begin = new DateTime();
        adapter.populate("http://epgadmin.tvblob.com/api/cielo/programmes/schedules/today.json");
        DateTime end = new DateTime();
        
        Period period = new Period(begin, end);
        System.out.println("Generated the playlist in " + period.getMinutes() + " minutes and " + period.getSeconds() + " seconds.");
    }
    
    public void testCanFetch() {
        assertTrue(adapter.canPopulate("http://epgadmin.tvblob.com/api/raiuno/programmes/schedules/today.json"));
    }
}
