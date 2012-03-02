package org.atlasapi.remotesite.lovefilm;

import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 */
public class LoveFilmUpdaterTest {

    public LoveFilmUpdaterTest() {
    }

    @Test
    public void testRunTask() {
        AdapterLog log = mock(AdapterLog.class);
        ContentWriter writer = mock(ContentWriter.class);

        LoveFilmUpdater updater = new LoveFilmUpdater(writer, log, "ubdxxqxe4nrnbdae8wt92pqe", "v7upKdKxCe");
        updater.runTask();
    }
}
