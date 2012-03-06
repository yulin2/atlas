package org.atlasapi.remotesite.lovefilm;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.junit.Ignore;
import static org.mockito.Mockito.*;

/**
 */
public class LoveFilmUpdaterTest {

    public LoveFilmUpdaterTest() {
    }

    @Ignore
    public void testRunTask() {
        AdapterLog log = mock(AdapterLog.class);
        ContentResolver resolver = mock(ContentResolver.class);
        ContentWriter writer = mock(ContentWriter.class);

        LoveFilmUpdater updater = new LoveFilmUpdater(resolver, writer, log, "", "");
        updater.runTask();
    }
}
