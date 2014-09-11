package org.atlasapi.remotesite.getty;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.scheduling.UpdateProgress;

public class GettyUpdateTask extends ScheduledTask {

    private static final Logger log = LoggerFactory.getLogger(GettyUpdateTask.class);
    
    private final GettyClient gettyClient;
    private final GettyAdapter adapter;
    private final GettyDataHandler dataHandler;
    
    private final IrisKeywordsFetcher keywordsFetcher;
    private final int gettyItemsPerPage;
    private final int irisItemsPerPage;
    
    public GettyUpdateTask(GettyClient gettyClient, GettyAdapter adapter, 
            GettyDataHandler dataHandler, IrisKeywordsFetcher keywordsFetcher,
            int gettyItemsPerPage, int irisItemsPerPage) {
        this.gettyClient = checkNotNull(gettyClient);
        this.adapter = checkNotNull(adapter);
        this.dataHandler = checkNotNull(dataHandler);
        
        this.keywordsFetcher = checkNotNull(keywordsFetcher);
        this.gettyItemsPerPage = gettyItemsPerPage;
        this.irisItemsPerPage = irisItemsPerPage;
    }
    
    @Override
    protected void runTask() {
        
        try {
            GettyDataProcessor<UpdateProgress> processor = processor();
            int offset = 0;
            
            
            List<String> keywords = keywordsFetcher.getKeywordsFromOffset(offset);
            
            //paginate keywords
            while (!keywords.isEmpty()) {
                for (String keyword : keywords) {
                    log.debug(String.format("Processing keyword %s", keyword));
                    processor.process(keyword);
                }
                offset += irisItemsPerPage;
                keywords = keywordsFetcher.getKeywordsFromOffset(offset);
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
            public boolean process(String keyword) {
                try {
                    int offset = 1;
                    String response = gettyClient.getVideoResponse(keyword, offset);
                    List<VideoResponse> videos = adapter.parse(response, keyword);
                    
                    //paginate videos
                    while (!videos.isEmpty()) {
                        for (VideoResponse video : videos) {
                            try {
                                dataHandler.handle(video);
                                progress = progress.reduce(UpdateProgress.SUCCESS);
                            } catch (Exception e) {
                                log.warn(String.format("Failed to get page of videos for keyword %s.", keyword), e);
                                progress = progress.reduce(UpdateProgress.FAILURE);
                            }
                        }
                        offset += gettyItemsPerPage;
                        response = gettyClient.getVideoResponse(keyword, offset);
                        videos = adapter.parse(response, keyword);
                    }
                    
                } catch (Exception e) {
                    log.warn(String.format("Failed to get videos for keyword %s.", keyword), e);
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
