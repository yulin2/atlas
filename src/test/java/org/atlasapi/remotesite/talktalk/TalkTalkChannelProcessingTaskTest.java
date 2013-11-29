package org.atlasapi.remotesite.talktalk;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.atlasapi.persistence.content.ContentGroupResolver;
import org.atlasapi.persistence.content.ContentGroupWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.remotesite.talktalk.vod.bindings.ChannelType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.scheduling.UpdateProgress;

@RunWith(MockitoJUnitRunner.class)
public class TalkTalkChannelProcessingTaskTest {

    @SuppressWarnings("unchecked")
    private TalkTalkChannelProcessor<UpdateProgress> processor = mock(TalkTalkChannelProcessor.class);
    private ContentGroupResolver contentGroupResolver = mock(ContentGroupResolver.class);
    private ContentGroupWriter contentGroupWriter = mock(ContentGroupWriter.class);
    
    private StubTalkTalkClient client = new StubTalkTalkClient();
    private TalkTalkChannelProcessingTask task = new TalkTalkChannelProcessingTask(client, processor,
            contentGroupResolver, contentGroupWriter);
    
    @Test
    public void testUpdatesContent() throws TalkTalkException {
        when(processor.process(argThat(is(any(ChannelType.class))), argThat(is(any(Optional.class)))))
            .thenReturn(UpdateProgress.SUCCESS);
        
        when(contentGroupResolver.findByCanonicalUris(ImmutableList.of(TalkTalkChannelProcessingTask.ALL_CONTENT_CONTENT_GROUP_URI)))
            .thenReturn(ResolvedContent.builder().build());
        
        task.run();
        
        verify(processor, times(1)).process(argThat(is(any(ChannelType.class))), argThat(is(any(Optional.class))));
        
        assertThat(task.getCurrentStatusMessage(), is(UpdateProgress.SUCCESS.toString()));
        
    }

}
