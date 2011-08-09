package org.atlasapi.remotesite.pa;

import java.io.File;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.pa.data.PaProgrammeDataStore;
import org.joda.time.DateTime;

import com.google.common.base.Predicate;
import com.metabroadcast.common.time.DateTimeZones;

public class PaRecentUpdater extends PaBaseProgrammeUpdater implements Runnable {
    
    private final PaProgrammeDataStore fileManager;

    public PaRecentUpdater(PaProgDataProcessor processor, PaProgrammeDataStore fileManager, AdapterLog log) {
        super(processor, fileManager, log, "recent");
        this.fileManager = fileManager;
    }
    
    @Override
    public void runTask() {
        final Long since = new DateTime(DateTimeZones.UTC).minusDays(2).getMillis();
        this.processFiles(fileManager.localFiles(new Predicate<File>() {
            @Override
            public boolean apply(File input) {
                return input.lastModified() > since;
            }
        }));
    }
}
