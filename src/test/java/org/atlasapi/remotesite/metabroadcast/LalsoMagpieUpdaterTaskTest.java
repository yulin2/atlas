package org.atlasapi.remotesite.metabroadcast;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Map;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.atlasapi.persistence.topic.TopicStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.base.Optional;
import com.google.common.io.Resources;

public class LalsoMagpieUpdaterTaskTest {

    private final SchedulingStore schedulingStore = mock(SchedulingStore.class);
    private final ContentResolver contentResolver = mock(ContentResolver.class);
    private final TopicStore topicStore = mock(TopicStore.class);
    private final TopicQueryResolver topicResolver = mock(TopicQueryResolver.class);
    private final ContentWriter contentWriter = mock(ContentWriter.class);
    
    private RemoteMagpieResultsSource source;
    private MetaBroadcastMagpieUpdater updater;
    private MagpieUpdaterTask task;
    
    @Before
    public void setup() {
        source = new FileMagpieResultsSource(new File(Resources.getResource("org/atlasapi/remotesite/lalso").getFile()));
        updater = new MetaBroadcastMagpieUpdater(contentResolver, topicStore, topicResolver, contentWriter, "ns", Publisher.LONDON_ALSO);
        task = new MagpieUpdaterTask(source, updater, schedulingStore);
    }
    
    @Test
    public void test() {

        String uri = "http://london.metabroadcast.com/paralympics.channel4.com/the-sports/archery/";

        when(schedulingStore.retrieveState(any(String.class)))
            .thenReturn(Optional.<Map<String,Object>>absent());
        when(contentResolver.findByCanonicalUris(argThat(hasItems(uri))))
            .thenReturn(ResolvedContent.builder().build());
        
        task.run();
        
        ArgumentCaptor<Brand> brandCaptor = ArgumentCaptor.forClass(Brand.class);
        verify(contentWriter, atLeast(1)).createOrUpdate(brandCaptor.capture());
        
        Brand written = brandCaptor.getValue();
        assertThat(written.getCanonicalUri(), startsWith("http://london.metabroadcast.com"));
        assertThat(written.getTitle(), is(not(nullValue())));
        assertThat(written.getImage(), is(not(nullValue())));
        assertThat(written.getKeyPhrases().isEmpty(), is(false));
    }

}
