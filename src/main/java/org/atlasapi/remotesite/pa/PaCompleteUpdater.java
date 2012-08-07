package org.atlasapi.remotesite.pa;

import java.io.File;

import org.atlasapi.persistence.media.channel.ChannelResolver;
import org.atlasapi.remotesite.pa.data.PaProgrammeDataStore;
import org.atlasapi.remotesite.pa.persistence.PaScheduleVersionStore;

import com.google.common.base.Optional;
import com.google.common.base.Predicates;

public class PaCompleteUpdater extends PaBaseProgrammeUpdater implements Runnable {
    
    private final PaProgrammeDataStore fileManager;

    public PaCompleteUpdater(PaChannelProcessor processor, PaProgrammeDataStore fileManager, ChannelResolver channelResolver) {
        super(processor, fileManager, channelResolver, Optional.<PaScheduleVersionStore>absent());
        this.fileManager = fileManager;
    }
    
    @Override
    public void runTask() {
        this.processFiles(fileManager.localFiles(Predicates.<File>alwaysTrue()));
    }
    
}
