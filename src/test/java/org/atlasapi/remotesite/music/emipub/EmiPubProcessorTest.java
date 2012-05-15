package org.atlasapi.remotesite.music.emipub;

import java.io.File;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 */
@Ignore
public class EmiPubProcessorTest {

    @Test
    public void testProcess() throws Exception {
        File data = new File("/Users/sergio/Desktop/emipub/emi_publishing_3.csv");

        AdapterLog log = mock(AdapterLog.class);
        ContentWriter contentWriter = mock(ContentWriter.class);

        EmiPubProcessor processor = new EmiPubProcessor();
        processor.process(data, log, contentWriter);
    }
}
