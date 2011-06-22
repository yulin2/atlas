package org.atlasapi.equiv;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentTable;
import org.atlasapi.persistence.content.listing.ContentLister;
import org.atlasapi.persistence.content.listing.ContentListingCriteria;
import org.atlasapi.persistence.content.listing.ContentListingHandler;
import org.atlasapi.persistence.content.listing.ContentListingProgress;
import org.atlasapi.persistence.content.mongo.ChildRefWriter;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class ChildRefUpdateTask extends ScheduledTask {

    private final ContentLister contentLister;
    private ChildRefWriter childRefWriter;

    public ChildRefUpdateTask(ContentLister contentLister, DatabasedMongo mongo) {
        this.contentLister = contentLister;
        this.childRefWriter = new ChildRefWriter(mongo);
    }

    @Override
    protected void runTask() {
        
        contentLister.listContent(ImmutableSet.of(ContentTable.CHILD_ITEMS), ContentListingCriteria.defaultCriteria(), new ContentListingHandler() {
            
            @Override
            public boolean handle(Content content, ContentListingProgress progress) {
                if(content instanceof Episode) {
                    childRefWriter.includeEpisodeInSeriesAndBrand((Episode)content);
                } else if(content instanceof Item) {
                    childRefWriter.includeItemInTopLevelContainer((Item)content);
                }
                reportStatus(progress.toString());
                return true;
            }
            
        });
        
    }

}
