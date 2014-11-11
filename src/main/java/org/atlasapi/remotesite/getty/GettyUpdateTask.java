package org.atlasapi.remotesite.getty;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.scheduling.UpdateProgress;

public class GettyUpdateTask extends ScheduledTask {
    private static final Logger log = LoggerFactory.getLogger(GettyUpdateTask.class);

    private static final String MAGIC_ALL_ITEMS_SEARCH_KEYWORD = "all vocabulary";

    private final GettyClient gettyClient;
    private final GettyAdapter adapter;
    private final GettyDataHandler dataHandler;
    private final ContentLister contentLister;

    private final int itemsPerPage;

    public GettyUpdateTask(GettyClient gettyClient, GettyAdapter adapter, 
            GettyDataHandler dataHandler, ContentLister contentLister,
            int itemsPerPage) {
        this.gettyClient = checkNotNull(gettyClient);
        this.adapter = checkNotNull(adapter);
        this.dataHandler = checkNotNull(dataHandler);
        this.contentLister = checkNotNull(contentLister);

        this.itemsPerPage = itemsPerPage;
    }

    @Override
    protected void runTask() {
        UpdateProgress progress = UpdateProgress.START;

        Set<String> receivedItemUris = new HashSet<String>();

        int offset = 1;  // Getty API starts its offsets at 1.

        //paginate videos
        while (true) {
            try {
                reportStatus(progress.toString());

                String response = null;
                try {
                    log.debug("Requesting Getty items from {}", offset);
                    response = gettyClient.getVideoResponse(MAGIC_ALL_ITEMS_SEARCH_KEYWORD, offset);
                } catch (IOException e) {
                    Throwables.propagate(e);
                }

                log.debug("Parsing response");
                List<VideoResponse> videos = adapter.parse(response);
                if (videos.isEmpty()) {
                    break;
                }

                for (VideoResponse video : videos) {
                    try {
                        log.debug("Writing item {} ({})", video.getAssetId(), video.getTitle());
                        Identified written = dataHandler.handle(video);
                        receivedItemUris.add(written.getCanonicalUri());
                        progress = progress.reduce(UpdateProgress.SUCCESS);
                    } catch (Exception e) {
                        log.warn(String.format("Failed to interpret a video response"), e);
                        progress = progress.reduce(UpdateProgress.FAILURE);
                    }
                }
            } catch (Exception e) {
                log.error("Whole batch failed for some reason", e);
            }
            offset += itemsPerPage;
        }

        // Everything we haven't seen this time around must be not activelyPublished.
        //
        Iterator<Content> allContent = contentLister.listContent(ContentListingCriteria.defaultCriteria().forPublisher(Publisher.GETTY).build());
        while (allContent.hasNext()) {
            Content item = allContent.next();
            if (! receivedItemUris.contains(item.getCanonicalUri())) {
                item.setActivelyPublished(false);
                dataHandler.write(item);
            }
        }
    }

}
