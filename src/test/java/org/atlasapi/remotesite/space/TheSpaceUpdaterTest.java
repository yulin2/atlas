package org.atlasapi.remotesite.space;

import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.junit.Ignore;
import static org.mockito.Mockito.*;

/**
 */
@Ignore
public class TheSpaceUpdaterTest {

    public TheSpaceUpdaterTest() {
    }

    public void testRunTask() throws Exception {
        AdapterLog log = mock(AdapterLog.class);
        ContentResolver resolver = mock(ContentResolver.class);
        ContentWriter writer = mock(ContentWriter.class);

        TheSpaceUpdater updater = new TheSpaceUpdater(resolver, writer, log, "/Users/sergio/atlas.jks", "sergio");
        updater.runTask();
    }
}
