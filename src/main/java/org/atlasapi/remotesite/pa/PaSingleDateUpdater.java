package org.atlasapi.remotesite.pa;

import java.io.File;

import org.atlasapi.persistence.media.channel.ChannelResolver;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.pa.data.PaProgrammeDataStore;
import org.atlasapi.remotesite.pa.persistence.PaScheduleVersionStore;
import org.springframework.stereotype.Controller;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

@Controller
public class PaSingleDateUpdater extends PaBaseProgrammeUpdater {
    
    private final String dateString;
    private final PaProgrammeDataStore fileManager;

    public PaSingleDateUpdater(PaChannelProcessor channelProcessor, AdapterLog log, PaProgrammeDataStore fileManager, ChannelResolver channelResolver, String dateString) {
        super(channelProcessor, fileManager, channelResolver, Optional.<PaScheduleVersionStore>absent());
        this.fileManager = fileManager;
        this.dateString = dateString;
    }

    @Override
    public void runTask() {
    	final String filenameContains = dateString + "_tvdata";
        processFiles(fileManager.localTvDataFiles(new Predicate<File>() {
            @Override
            public boolean apply(File input) {
                return input.getName().contains(filenameContains);
            }
        }));
    }
}
