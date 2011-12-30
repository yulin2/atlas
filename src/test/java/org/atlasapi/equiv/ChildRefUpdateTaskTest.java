package org.atlasapi.equiv;

import static org.atlasapi.media.entity.Publisher.BBC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import junit.framework.TestCase;

import org.atlasapi.equiv.update.tasks.ScheduleTaskProgressStore;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.ChildRef;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.SortKey;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingCriteria;
import org.atlasapi.persistence.logging.NullAdapterLog;
import org.atlasapi.persistence.media.entity.ContainerTranslator;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.persistence.MongoTestHelper;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;

@RunWith(JMock.class)
public class ChildRefUpdateTaskTest extends TestCase {

    private final Mockery context = new Mockery();
    ScheduleTaskProgressStore progressStore = context.mock(ScheduleTaskProgressStore.class);
    ContentResolver resolver = context.mock(ContentResolver.class);
    ContentLister lister = context.mock(ContentLister.class);
    
    DatabasedMongo mongo = MongoTestHelper.anEmptyTestDatabase();
    
    ChildRefUpdateTask task = new ChildRefUpdateTask(lister, resolver, mongo, progressStore, new NullAdapterLog()).forPublishers(BBC);
    
    ContainerTranslator translator = new ContainerTranslator(new SubstitutionTableNumberCodec());
    
    public void testUpdatesChildRefSortKeys() {
        
        final String containerUri = "brandUri";
        
        final String ep1Uri = "ep1";
        final Episode ep1 = new Episode(ep1Uri, "c"+ep1Uri, BBC);
        ep1.setSeriesNumber(5);
        ep1.setEpisodeNumber(5);
        
        final String ep2Uri = "ep2";
        final Episode ep2 = new Episode(ep2Uri, "c"+ep2Uri, BBC);
        ep2.setSeriesNumber(5);
        ep2.setEpisodeNumber(6);
        
        context.checking(new Expectations(){{
            ignoring(progressStore);
            one(lister).listContent(with(any(ContentListingCriteria.class))); will(returnValue(Iterators.forArray(aContainer(containerUri, ep1.childRef(), ep2.childRef()))));
            one(resolver).findByCanonicalUris(with(hasItems(ep1Uri, ep2Uri))); will(returnValue(ResolvedContent.builder().put(ep1Uri, ep1).put(ep2Uri, ep2).build()));
        }});

        task.run();
        
        Container written = translator.fromDB(mongo.collection("containers").findOne(containerUri));
        
        assertThat(written.getTitle(), is(notNullValue()));
        
        ImmutableList<ChildRef> childRefs = written.getChildRefs();
        assertThat(childRefs.size(),is(2));
        assertThat(childRefs.get(0).getSortKey(), is(SortKey.keyFrom(ep2)));
        assertThat(childRefs.get(1).getSortKey(), is(SortKey.keyFrom(ep1)));
    }

    private Brand aContainer(String id, ChildRef... children) {
        Brand brand = new Brand(id, "c"+id, BBC);
        brand.setTitle("a title");
        brand.setChildRefs(ImmutableList.copyOf(children));
        return brand;
    }
}
