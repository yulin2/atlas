package org.atlasapi.remotesite.channel4.epg;

import static org.atlasapi.media.entity.Channel.CHANNEL_FOUR;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasValue;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;

import org.atlasapi.media.entity.Channel;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Schedule;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ScheduleResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.NullAdapterLog;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.persistence.testing.StubContentResolver;
import org.atlasapi.remotesite.channel4.C4BrandUpdater;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.metabroadcast.common.time.DateTimeZones;
import com.metabroadcast.common.time.DayRangeGenerator;

public class C4EpgUpdaterTest extends TestCase {
    
    private final Mockery context = new Mockery();
    
    private final Builder builder = new Builder(new C4EpgElementFactory());
    private Document c4EpgFeed;
    
    @SuppressWarnings("unchecked")
    private final RemoteSiteClient<Document> c4AtomFetcher = context.mock(RemoteSiteClient.class);
    private final ContentWriter contentWriter = context.mock(ContentWriter.class);
    private final C4BrandUpdater brandUpdater = context.mock(C4BrandUpdater.class);
    private final DateTime day = new DateTime();
    
    private final AdapterLog log = new NullAdapterLog();
    private final ContentResolver resolver = StubContentResolver.RESOLVES_NOTHING;
    
    private final BroadcastTrimmer trimmer = context.mock(BroadcastTrimmer.class);
    
    private final C4EpgUpdater updater = new C4EpgUpdater(c4AtomFetcher, new C4EpgEntryProcessor(contentWriter, resolver, brandUpdater, log), new C4EpgBrandlessEntryProcessor(contentWriter, resolver, brandUpdater, log), trimmer, log, new DayRangeGenerator());
    
    @Override
    public void setUp() throws Exception {
        c4EpgFeed = builder.build(new InputStreamReader(Resources.getResource("c4-epg-2011-01-07.atom").openStream()));
    }

    @SuppressWarnings("unchecked")
    public void testRun() throws Exception {
        
        context.checking(new Expectations() {{
            one(c4AtomFetcher).get(with(endsWith(String.format("%s/C4.atom", new DateTime(DateTimeZones.UTC).toString("yyyy/MM/dd")))));
                will(returnValue(c4EpgFeed));
            allowing(c4AtomFetcher).get(with(any(String.class)));
                will(returnValue(new Document(new Element("feed"))));
            allowing(brandUpdater).createOrUpdateBrand(with(any(String.class))); will(throwException(new RuntimeException()));
            allowing(contentWriter).createOrUpdate(with(any(Container.class)));
            allowing(contentWriter).createOrUpdate(with(any(Item.class)));
            one(trimmer).trimBroadcasts(with(new Interval(day, day.plusDays(1))), with(CHANNEL_FOUR), (Map<String,String>)with(allOf(hasKey(any(String.class)),hasValue("http://www.channel4.com/programmes/the-hoobs/episode-guide/series-1/episode-58"))));
        }});
        
        updater.run();
        
        context.assertIsSatisfied();
        
    }
}
