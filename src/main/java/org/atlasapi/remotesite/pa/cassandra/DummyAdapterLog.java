package org.atlasapi.remotesite.pa.cassandra;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;

/**
 */
public class DummyAdapterLog implements AdapterLog{

    @Override
    public void record(AdapterLogEntry entry) {
        System.out.println(entry.toString());
    }
    
}
