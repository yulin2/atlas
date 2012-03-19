package org.atlasapi.remotesite.lovefilm;

import java.util.Collections;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.logging.AdapterLog;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 */
@Ignore
public class LoveFilmUpdaterTest {

    public LoveFilmUpdaterTest() {
    }

    @Test
    public void testRunTask() {
        AdapterLog log = mock(AdapterLog.class);
        ContentResolver resolver = mock(ContentResolver.class);
        ContentWriter writer = mock(ContentWriter.class);

        when(resolver.findByCanonicalUris(anyCollection())).thenReturn(new ResolvedContent(Collections.EMPTY_MAP));

        LoveFilmUpdater updater = new LoveFilmUpdater(resolver, writer, log, "", "");
        updater.runTask();
    }
}
