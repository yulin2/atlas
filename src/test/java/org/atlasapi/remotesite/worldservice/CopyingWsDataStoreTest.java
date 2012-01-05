package org.atlasapi.remotesite.worldservice;

import java.io.IOException;

import junit.framework.TestCase;

import org.atlasapi.persistence.logging.NullAdapterLog;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.time.DateTimeZones;

@RunWith(JMock.class)
public class CopyingWsDataStoreTest extends TestCase {

    private final Mockery context = new Mockery();
    private final WsDataStore remote = context.mock(WsDataStore.class);
    private final WritableWsDataStore local = context.mock(WritableWsDataStore.class);

    private final CopyingWsDataStore store = new CopyingWsDataStore(remote, local, new NullAdapterLog());

    @Test
    public void testGettingLatestDataIsNothingWhenLocalAndRemoteReturnNothing() {
        
        context.checking(new Expectations(){{
            one(remote).latestData();will(returnValue(Maybe.nothing()));
            one(local).latestData();will(returnValue(Maybe.nothing()));
        }});
        
        assertTrue(store.latestData().isNothing());
        
    }

    @Test
    public void testGettingLatestDataIsLocalWhenRemoteReturnsNothing() {
        
        final WsDataSet testSet = new WsDataSet() {
            
            @Override
            public DateTime getVersion() {
                return null;
            }
            
            @Override
            public WsDataSource getDataForFile(WsDataFile file) {
                return null;
            }
        };
        
        context.checking(new Expectations(){{
            one(remote).latestData();will(returnValue(Maybe.nothing()));
            one(local).latestData();will(returnValue(Maybe.just(testSet)));
        }});
        
        Maybe<WsDataSet> latestData = store.latestData();
        assertTrue(latestData.requireValue() == testSet);
        
    }

    @Test
    public void testGettingLatestDataIsLocalWhenRemoteReturnsSameVersion() {
        DateTime day = new DateTime(DateTimeZones.UTC);
        final WsDataSet localSet = setFor(day);
        final WsDataSet remoteSet = setFor(day);
        
        context.checking(new Expectations(){{
            one(remote).latestData();will(returnValue(Maybe.just(remoteSet)));
            one(local).latestData();will(returnValue(Maybe.just(localSet)));
        }});
        
        Maybe<WsDataSet> latestData = store.latestData();
        assertTrue(latestData.requireValue() == localSet);
        
    }

    @Test
    public void testGettingLatestDataIsWrittenLocallyWhenLocalReturnsNothing() throws IOException {
        DateTime day = new DateTime(DateTimeZones.UTC);
        final WsDataSet localSet = setFor(day);
        final WsDataSet remoteSet = setFor(day);
        
        context.checking(new Expectations(){{
            one(remote).latestData();will(returnValue(Maybe.just(remoteSet)));
            one(local).latestData();will(returnValue(Maybe.nothing()));
            one(local).write(remoteSet); will(returnValue(localSet));
        }});
        
        Maybe<WsDataSet> latestData = store.latestData();
        assertTrue(latestData.requireValue() == localSet);
        
    }

    @Test
    public void testGettingLatestDataIsWrittenLocallyWhenRemoteVersionIsMoreRecent() throws IOException {
        DateTime day = new DateTime(DateTimeZones.UTC);
        final WsDataSet localSet = setFor(day);
        final WsDataSet remoteSet = setFor(day.plusDays(1));
        final WsDataSet writtenSet = setFor(day.plusDays(1));
        
        context.checking(new Expectations(){{
            one(remote).latestData();will(returnValue(Maybe.just(remoteSet)));
            one(local).latestData();will(returnValue(Maybe.just(localSet)));
            one(local).write(remoteSet); will(returnValue(writtenSet));
        }});
        
        Maybe<WsDataSet> latestData = store.latestData();
        assertTrue(latestData.requireValue() == writtenSet);
        
    }

    @Test
    public void testGettingDateForDayReturnsOnlyLocalWhenAvailable() throws IOException {
        final DateTime day = new DateTime(DateTimeZones.UTC);
        final WsDataSet localSet = setFor(day);
        
        context.checking(new Expectations(){{
            one(local).dataForDay(day);will(returnValue(Maybe.just(localSet)));
            never(remote).dataForDay(day);
            never(local).write(with(any(WsDataSet.class)));
        }});
        
        Maybe<WsDataSet> dayData = store.dataForDay(day);
        assertTrue(dayData.requireValue() == localSet);
        
    }

    @Test
    public void testGettingDateForDayGetsRemoteAndDoesntWriteIfNothing() throws IOException {
        final DateTime day = new DateTime(DateTimeZones.UTC);
        
        context.checking(new Expectations(){{
            one(local).dataForDay(day);will(returnValue(Maybe.nothing()));
            one(remote).dataForDay(day);will(returnValue(Maybe.nothing()));
            never(local).write(with(any(WsDataSet.class)));
        }});
        
        Maybe<WsDataSet> dayData = store.dataForDay(day);
        assertTrue(dayData.isNothing());
        
    }

    @Test
    public void testGettingDateForDayWritesRemoteDataLocally() throws IOException {
        final DateTime day = new DateTime(DateTimeZones.UTC);
        final WsDataSet localSet = setFor(day);
        final WsDataSet remoteSet = setFor(day);
        
        context.checking(new Expectations(){{
            one(local).dataForDay(day);will(returnValue(Maybe.nothing()));
            one(remote).dataForDay(day);will(returnValue(Maybe.just(remoteSet)));
            one(local).write(remoteSet);will(returnValue(localSet));
        }});
        
        Maybe<WsDataSet> dayData = store.dataForDay(day);
        assertTrue(dayData.requireValue() == localSet);
        
    }
    
    private WsDataSet setFor(final DateTime day) {
        return new WsDataSet() {
            
            @Override
            public DateTime getVersion() {
                return day;
            }
            
            @Override
            public WsDataSource getDataForFile(WsDataFile file) {
                return null;
            }
        };
    }
    
    
}
