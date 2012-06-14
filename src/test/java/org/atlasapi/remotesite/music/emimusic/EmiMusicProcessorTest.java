package org.atlasapi.remotesite.music.emimusic;

import com.google.common.io.Files;
import com.metabroadcast.common.base.Maybe;
import java.io.File;
import java.util.Arrays;
import org.atlasapi.media.entity.Song;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.s3.S3Client;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 */
public class EmiMusicProcessorTest {

    @Test
    public void testProcess() throws Exception {
        S3Client client = mock(S3Client.class);
        when(client.list()).thenReturn(Arrays.asList("emimusic1.xml", "emimusic2.xml"));
        when(client.getAndSaveIfUpdated(eq("emimusic1.xml"), any(File.class), any(Maybe.class))).thenAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                File f = (File) invocation.getArguments()[1];
                Files.copy(new File(EmiMusicProcessorTest.class.getClassLoader().getResource("emimusic1.xml").getFile()), f);
                return Boolean.TRUE;
            }
        });
        when(client.getAndSaveIfUpdated(eq("emimusic2.xml"), any(File.class), any(Maybe.class))).thenAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                File f = (File) invocation.getArguments()[1];
                Files.copy(new File(EmiMusicProcessorTest.class.getClassLoader().getResource("emimusic2.xml").getFile()), f);
                return Boolean.TRUE;
            }
        });
        
        ContentWriter writer = mock(ContentWriter.class);
        ArgumentCaptor<Song> captor = ArgumentCaptor.forClass(Song.class);
        
        AdapterLog log = mock(AdapterLog.class);
        
        EmiMusicProcessor processor = new EmiMusicProcessor();
        processor.process(client, writer, log);
        
        verify(writer, times(3)).createOrUpdate(captor.capture());
        
        assertEquals(3, captor.getAllValues().size());
    }
}
