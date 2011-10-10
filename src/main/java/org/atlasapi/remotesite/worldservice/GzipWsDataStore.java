package org.atlasapi.remotesite.worldservice;

import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.joda.time.DateTime;

import com.metabroadcast.common.base.Maybe;

public class GzipWsDataStore implements WsDataStore {

    private final WsDataStore delegate;

    public GzipWsDataStore(WsDataStore delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public Maybe<WsDataSet> latestData() {
        return gzipSet(delegate.latestData());
    }
    
    @Override
    public Maybe<WsDataSet> dataForDay(DateTime day) {
        return gzipSet(delegate.dataForDay(day));
    }

    private Maybe<WsDataSet> gzipSet(Maybe<WsDataSet> set) {
        if(set.isNothing()) {
            return set;
        }
        return Maybe.just(gzipSet(set.requireValue()));
    }
    
    private WsDataSet gzipSet(final WsDataSet set) {
        return new WsDataSet() {
            
            @Override
            public DateTime getVersion() {
                return set.getVersion();
            }
            
            @Override
            public WsDataSource getDataForFile(WsDataFile file) {
                try {
                    WsDataSource source = set.getDataForFile(file);
                    return new WsDataSource(source.file(), new GZIPInputStream(source.data()));
                } catch (IOException e) {
                    return null;
                }
            }
        };
    }
    
}
