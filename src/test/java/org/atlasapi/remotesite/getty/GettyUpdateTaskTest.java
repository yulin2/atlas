package org.atlasapi.remotesite.getty;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.atlasapi.media.entity.Content;
import org.junit.Test;
import org.mockito.Matchers;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class GettyUpdateTaskTest {

    private final GettyAdapter adapter = mock(GettyAdapter.class);
    private final GettyDataHandler handler = mock(GettyDataHandler.class);
    private final GettyTokenFetcher tokenFetcher = mock(GettyTokenFetcher.class);
    private final GettyClient gettyClient = mock(GettyClient.class);
    private final ScheduledTask task = new GettyUpdateTask(gettyClient, adapter, handler, 90);

    private final VideoResponse res1 = new VideoResponse(), res2 = new VideoResponse();
    private final ImmutableList<VideoResponse> mockedClientResponses = ImmutableList.of(res1, res2);

    @Test
    public void testTask() throws Exception {
        res1.setAssetId("9842094");
        res2.setAssetId("eoduhnd");
        when(tokenFetcher.getToken()).thenReturn("token");
        when(gettyClient.getVideoResponse(Matchers.anyString(), Matchers.anyInt())).thenReturn("imagine json");
        when(adapter.parse(Matchers.anyString()))
           .thenReturn(mockedClientResponses)
           .thenReturn(ImmutableList.<VideoResponse>of());
        
        Content c1 = mock(Content.class);
        Content c2 = mock(Content.class);
        Content c3 = mock(Content.class);
        when(handler.handle(Matchers.any(VideoResponse.class)))
                .thenReturn(c1)
                .thenReturn(c2);
        task.run();
        
        verify(adapter, times(2)).parse(Matchers.anyString());
        verify(handler, times(2)).handle(Matchers.any(VideoResponse.class));
    }
    
}
