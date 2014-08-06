package org.atlasapi.remotesite.getty;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.scheduling.UpdateProgress;

public class GettyUpdateTask extends ScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(GettyUpdateTask.class);

    private final GettyAdapter adapter;
    private final GettyDataHandler dataHandler;
    private final GettyTokenFetcher tokenFetcher;
    private final GettyVideoFetcher videoFetcher;
    
    public GettyUpdateTask(GettyAdapter adapter, GettyDataHandler dataHandler, GettyTokenFetcher tokenFetcher,
            GettyVideoFetcher videoFetcher) {
        this.adapter = checkNotNull(adapter);
        this.dataHandler = checkNotNull(dataHandler);
        this.tokenFetcher = checkNotNull(tokenFetcher);
        this.videoFetcher = checkNotNull(videoFetcher);
    }
    
    @Override
    protected void runTask() {
        
        try {
            List<String> keywords = ImmutableList.of();
            
            String oauthResponse = tokenFetcher.oauth();
            String token = tokenFetcher.getToken(oauthResponse);
            
            GettyDataProcessor<UpdateProgress> processor = processor();
            for (String keyword : keywords) {
                String response = videoFetcher.getResponse(token, keyword, 1);
                processor.process(response);
            }
            
            reportStatus(processor.getResult().toString());
            
        } catch (Exception e) {
            reportStatus(e.getMessage());
            throw Throwables.propagate(e);
        }
        
    }
    
    private GettyDataProcessor<UpdateProgress> processor() {
        return new GettyDataProcessor<UpdateProgress>() {
            
            UpdateProgress progress = UpdateProgress.START;
            
            @Override
            public boolean process(String response) {
                try {
                    List<VideoResponse> videos = adapter.parse(response);
                    
                    for (VideoResponse video : videos) {
                        dataHandler.handle(video);
                        progress = progress.reduce(UpdateProgress.SUCCESS);
                    }
                } catch (Exception e) {
                    log.warn("Failed to get videos.", e);
                    progress = progress.reduce(UpdateProgress.FAILURE);
                }
                
                reportStatus(progress.toString());
                return shouldContinue();
            }
            
            @Override
            public UpdateProgress getResult() {
                return progress;
            }
        };
    }
    
}
