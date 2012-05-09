package org.atlasapi.remotesite.music.musicbrainz;

import java.io.File;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.junit.Test;
import org.junit.Ignore;
import static org.mockito.Mockito.*;

/**
 */
@Ignore
public class MusicBrainzProcessorTest {

    @Test
    public void testProcess() throws Exception {
        File data = new File("/Users/sergio/Desktop/mbdump");

        AdapterLog log = mock(AdapterLog.class);
        ContentWriter contentWriter = mock(ContentWriter.class);
        ItemsPeopleWriter peopleWriter = mock(ItemsPeopleWriter.class);

        //CassandraContentStore contentWriter = new CassandraContentStore(Arrays.asList("127.0.0.1"), 9160, 10, 10000, 10000);
        //contentWriter.init();

        MusicBrainzProcessor processor = new MusicBrainzProcessor();
        processor.process(data, log, contentWriter, peopleWriter);
    }
}
