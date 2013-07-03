package org.atlasapi.remotesite.talktalk;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.atlasapi.remotesite.talktalk.vod.bindings.ChannelType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.metabroadcast.common.scheduling.UpdateProgress;

@RunWith(MockitoJUnitRunner.class)
public class TalkTalkChannelProcessingTaskTest {

    @SuppressWarnings("unchecked")
    private TalkTalkChannelProcessor<UpdateProgress> processor = mock(TalkTalkChannelProcessor.class);
    private StubTalkTalkClient client = new StubTalkTalkClient();
    private TalkTalkChannelProcessingTask task = new TalkTalkChannelProcessingTask(client, processor);
    
    @Test
    public void testUpdatesContent() throws TalkTalkException {
        
        when(processor.process(argThat(is(any(ChannelType.class)))))
            .thenReturn(UpdateProgress.SUCCESS);
        
        task.run();
        
        verify(processor, times(1)).process(argThat(is(any(ChannelType.class))));
        
        assertThat(task.getCurrentStatusMessage(), is(UpdateProgress.SUCCESS.toString()));
        
    }

}
