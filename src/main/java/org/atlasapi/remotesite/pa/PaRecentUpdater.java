package org.atlasapi.remotesite.pa;

import java.util.concurrent.ExecutorService;

import org.atlasapi.feeds.upload.FileUploadResult;
import org.atlasapi.feeds.upload.persistence.FileUploadResultStore;
import org.atlasapi.media.channel.ChannelResolver;
import org.atlasapi.remotesite.pa.data.PaProgrammeDataStore;
import org.atlasapi.remotesite.pa.persistence.PaScheduleVersionStore;
import org.joda.time.DateTime;

import com.google.common.base.Optional;
import com.metabroadcast.common.time.DateTimeZones;

public class PaRecentUpdater extends PaBaseProgrammeUpdater implements Runnable {
       
    private final PaProgrammeDataStore fileManager;
    final FileUploadResultStore fileUploadResultStore;
    
    public PaRecentUpdater(ExecutorService executor, PaChannelProcessor channelProcessor, PaProgrammeDataStore fileManager, ChannelResolver channelResolver, FileUploadResultStore fileUploadResultStore, PaScheduleVersionStore paScheduleVersionStore) {
        super(executor, channelProcessor, fileManager, channelResolver, Optional.of(paScheduleVersionStore));
        this.fileManager = fileManager;
        this.fileUploadResultStore = fileUploadResultStore;
    }
    
    @Override
    public void runTask() {
        final Long since = new DateTime(DateTimeZones.UTC).minusDays(10).getMillis();
        this.processFiles(fileManager.localTvDataFiles(new UnprocessedFileFilter(fileUploadResultStore, SERVICE, since)));
    }
    
    @Override 
    protected void storeResult(FileUploadResult result) {
        fileUploadResultStore.store(result.filename(), result);
    }
}
