//package org.atlasapi.remotesite.metabroadcast;
//
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import java.util.Map;
//
//import org.atlasapi.remotesite.metabroadcast.MagpieResults.MagpieResultsBuider;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.runners.MockitoJUnitRunner;
//
//import com.google.common.base.Optional;
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.ImmutableMap;
//import com.metabroadcast.common.time.Timestamp;
//
//@RunWith(MockitoJUnitRunner.class)
//public class MagpieUpdaterTaskTest {
//
//    private final RemoteMagpieResultsSource source = mock(RemoteMagpieResultsSource.class);
//    private final MetaBroadcastMagpieUpdater updater = mock(MetaBroadcastMagpieUpdater.class);
//    private final SchedulingStore store = mock(SchedulingStore.class);
//    
//    private final MagpieUpdaterTask task = new MagpieUpdaterTask(source, updater, store);
//    
//    @Test
//    public void testRunningTask() {
//
//        String taskName = "TaskName";
//        task.withName(taskName);
//        Timestamp timestamp = Timestamp.of(1234L);
//        MagpieResults results = rawResults();
//        
//        when(store.retrieveState(taskName))
//            .thenReturn(Optional.of((Map<String,Object>)ImmutableMap.<String,Object>of("lastModifiedTime", timestamp.millis())));
//        when(source.resultsChangeSince(timestamp))
//            .thenReturn(ImmutableList.of(remoteMagpieResults(results)));
//        
//        task.run();
//        
//        verify(updater).updateTopics(results);
//        verify(store).storeState(taskName, ImmutableMap.<String,Object>of("lastModifiedTime", 2000L));
//    }
//
//    private RemoteMagpieResults remoteMagpieResults(MagpieResults results) {
//        return RemoteMagpieResults.retrieved(results, Timestamp.of(2000L));
//    }
//
//    private MagpieResults rawResults() {
//        MagpieScheduleItem scheduleItem = MagpieScheduleItem.builder()
//                .withTitle("title")
//                .build();
//        MagpieResultsBuider resultsBuilder = new MagpieResultsBuider();
//        resultsBuilder.addResult(scheduleItem);
//        return resultsBuilder.build();
//    }
//
//}
