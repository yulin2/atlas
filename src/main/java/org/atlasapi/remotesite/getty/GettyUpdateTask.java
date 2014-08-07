package org.atlasapi.remotesite.getty;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.scheduling.UpdateProgress;

public class GettyUpdateTask extends ScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(GettyUpdateTask.class);
    private static final String EXPIRED_TOKEN_CODE = "AUTH-012";
    
    private final GettyAdapter adapter;
    private final GettyDataHandler dataHandler;
    private final GettyTokenFetcher tokenFetcher;
    private final GettyVideoFetcher videoFetcher;
    private final IrisKeywordsFetcher keywordsFetcher;
    private final int itemsPerPage;
    
    private String token;
    
    public GettyUpdateTask(GettyAdapter adapter, GettyDataHandler dataHandler, GettyTokenFetcher tokenFetcher,
            GettyVideoFetcher videoFetcher, IrisKeywordsFetcher keywordsFetcher, int itemsPerPage) {
        this.adapter = checkNotNull(adapter);
        this.dataHandler = checkNotNull(dataHandler);
        this.tokenFetcher = checkNotNull(tokenFetcher);
        this.videoFetcher = checkNotNull(videoFetcher);
        this.keywordsFetcher = checkNotNull(keywordsFetcher);
        this.itemsPerPage = itemsPerPage;
    }
    
    @Override
    protected void runTask() {
        
        try {
            GettyDataProcessor<UpdateProgress> processor = processor();
            int offset = 0;
            this.token = tokenFetcher.getToken();
            
            List<String> keywords = keywordsFetcher.getKeywordsFromOffset(offset);
            
            //paginate keywords
            while (!keywords.isEmpty()) {
                for (String keyword : keywords) {
                    processor.process(keyword);
                }
                offset += itemsPerPage;
                keywords = keywordsFetcher.getKeywordsFromOffset(offset);
            }
            
            reportStatus(processor.getResult().toString());
            
        } catch (Exception e) {
            reportStatus(e.getMessage());
            throw Throwables.propagate(e);
        }
        
    }
    
    private String getVideoResponse(String keyword, int offset) throws ClientProtocolException, IOException {
        String response = videoFetcher.getResponse(token, keyword, offset);
        if (response.contains(EXPIRED_TOKEN_CODE)) {
            this.token = tokenFetcher.getToken();
            return videoFetcher.getResponse(token, keyword, offset);
        }
        return response;
    }
    
    private GettyDataProcessor<UpdateProgress> processor() {
        return new GettyDataProcessor<UpdateProgress>() {
            
            UpdateProgress progress = UpdateProgress.START;
            
            @Override
            public boolean process(String keyword) {
                try {
                    int offset = 1;
                    String response = getVideoResponse(keyword, offset);
                    List<VideoResponse> videos = adapter.parse(response);
                    
                    //paginate videos
                    while (!videos.isEmpty()) {
                        for (VideoResponse video : videos) {
                            dataHandler.handle(video);
                            progress = progress.reduce(UpdateProgress.SUCCESS);
                        }
                        offset += itemsPerPage;
                        response = getVideoResponse(keyword, offset);
                        videos = adapter.parse(response);
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
