package org.atlasapi.remotesite.music.musicbrainz;

import java.io.File;
import java.util.Arrays;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.cassandra.CassandraContentStore;
import org.atlasapi.persistence.content.people.ItemsPeopleWriter;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.junit.Ignore;
import static org.mockito.Mockito.*;

/**
 */
@Ignore
public class MusicBrainzProcessorTest {

    @Test
    public void testProcess() throws Exception {
        File data = new File("/Users/sergio/Desktop/mbdump");

        CassandraContentStore contentWriter = new CassandraContentStore(Arrays.asList("127.0.0.1"), 9160, 10, 10000, 10000);
        //ItemsPeopleWriter peopleWriter = mock(ItemsPeopleWriter.class);
        
        contentWriter.init();

        MusicBrainzProcessor processor = new MusicBrainzProcessor();
        processor.process(data, contentWriter, null);
    }
}
