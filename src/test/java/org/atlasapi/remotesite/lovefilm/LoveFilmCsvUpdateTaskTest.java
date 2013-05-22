package org.atlasapi.remotesite.lovefilm;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.atlasapi.remotesite.lovefilm.LoveFilmData.LoveFilmDataRow;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.io.CharStreams;
import com.metabroadcast.common.scheduling.ScheduledTask;

@RunWith(MockitoJUnitRunner.class)
public class LoveFilmCsvUpdateTaskTest {

//    private LoveFilmDataSupplier dataSupplier = mock(LoveFilmDataSupplier.class);
    private LoveFilmDataRowHandler dataHandler = mock(LoveFilmDataRowHandler.class);
    private LoveFilmFileUpdater updater = mock(LoveFilmFileUpdater.class);
    private LoveFilmFileStore store = mock(LoveFilmFileStore.class);
    private LoveFilmBrandProcessor brandProcessor = mock(LoveFilmBrandProcessor.class);
    private final ScheduledTask task = new LoveFilmCsvUpdateTask(updater, store, dataHandler, brandProcessor);
    
    @Test
    public void testUpdateTask() {
        
        LoveFilmData data = new LoveFilmData(CharStreams.newReaderSupplier("\"header\",\"row\"\n\"value\",\"row\""));
        when(store.fetchLatestData()).thenReturn(data);
        
        task.run();
        
        ArgumentCaptor<LoveFilmDataRow> rowCaptor = ArgumentCaptor.forClass(LoveFilmDataRow.class);
        
        verify(brandProcessor).prepare();
        verify(brandProcessor, times(1)).handle(rowCaptor.capture());
        verify(brandProcessor).finish();
        
        verify(dataHandler).prepare();
        verify(dataHandler, times(1)).handle(rowCaptor.capture());
        
        LoveFilmDataRow row = rowCaptor.getValue();
        
        assertThat(row.getColumnValue("header"),is("value"));
        assertThat(row.getColumnValue("row"),is("row"));
        
    }

}
