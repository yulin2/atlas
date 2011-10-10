package org.atlasapi.remotesite.worldservice;

import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.io.IOException;

import org.atlasapi.persistence.logging.AdapterLog;
import org.joda.time.DateTime;

import com.metabroadcast.common.base.Maybe;

public class CopyingWsDataStore implements WsDataStore {

    private final WritableWsDataStore local;
    private final WsDataStore remote;
    private final AdapterLog log;

    public CopyingWsDataStore(WsDataStore remote, WritableWsDataStore local, AdapterLog log) {
        this.remote = remote;
        this.local = local;
        this.log = log;
    }

    @Override
    public Maybe<WsDataSet> latestData() {

        Maybe<WsDataSet> possibleLocalData = local.latestData();
        Maybe<WsDataSet> possibleRemoteData = remote.latestData();

        /*
         * This is arguable. The remote failed so there might be more recent,
         * but currently unaccessible, data. localData can still be nothing if
         * it has failed/has no data.
         */
        if (possibleRemoteData.isNothing()) {
            return possibleLocalData;
        }

        WsDataSet remoteData = possibleRemoteData.requireValue();

        // We have remote data but not local, or remote more recent than local.
        // Copy to local, return that.
        if (possibleLocalData.isNothing() || remoteData.getVersion().isAfter(possibleLocalData.requireValue().getVersion())) {
            return writeLocallyAndReturn(remoteData);
        }

        // local data up to date.
        return possibleLocalData;
    }

    private Maybe<WsDataSet> writeLocallyAndReturn(WsDataSet remoteData) {
        try {
            return Maybe.fromPossibleNullValue(local.write(remoteData));
        } catch (IOException e) {
            log.record(warnEntry().withCause(e).withDescription("Failed to copy remote data to local store").withSource(getClass()));
            return Maybe.nothing();
        }
    }

    @Override
    public Maybe<WsDataSet> dataForDay(DateTime day) {
        Maybe<WsDataSet> possibleLocalData = local.dataForDay(day);

        // Got local data for the day \0/
        if (possibleLocalData.hasValue()) {
            return possibleLocalData;
        }

        Maybe<WsDataSet> possibleRemoteData = remote.dataForDay(day);

        //Can't get local or remote data.
        if (possibleRemoteData.isNothing()) {
            return possibleRemoteData;
        }

        return writeLocallyAndReturn(possibleRemoteData.requireValue());
    }
}
