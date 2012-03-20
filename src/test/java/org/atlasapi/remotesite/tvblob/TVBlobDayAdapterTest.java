package org.atlasapi.remotesite.tvblob;

import junit.framework.TestCase;

import org.atlasapi.media.content.Brand;
import org.atlasapi.media.content.ContentResolver;
import org.atlasapi.media.content.ContentWriter;
import org.atlasapi.media.content.Episode;
import org.atlasapi.media.content.ResolvedContent;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class TVBlobDayAdapterTest extends TestCase {

    private final Mockery context = new Mockery();
    private ContentResolver resolver = context.mock(ContentResolver.class);
    private ContentWriter writer = context.mock(ContentWriter.class);
    private TVBlobDayAdapter adapter = new TVBlobDayAdapter(writer, resolver);

    public void testShouldRetrieveToday() throws Exception {
        context.checking(new Expectations() {{
            allowing(resolver).findByCanonicalUris(with(Expectations.<Iterable<String>>anything())); will(returnValue(ResolvedContent.builder().build()));
            allowing(writer).createOrUpdate((Episode) with(anything()));
            allowing(writer).createOrUpdate((Brand) with(anything()));
        }});
        
        DateTime begin = new DateTime();
        adapter.populate("http://epgadmin.tvblob.com/api/cielo/programmes/schedules/today.json");
        DateTime end = new DateTime();
        
        Period period = new Period(begin, end);
        System.out.println("Generated the playlist in " + period.getMinutes() + " minutes and " + period.getSeconds() + " seconds.");
    }

    @Test
    public void testCanFetch() {
        assertTrue(adapter.canPopulate("http://epgadmin.tvblob.com/api/raiuno/programmes/schedules/today.json"));
    }
}
