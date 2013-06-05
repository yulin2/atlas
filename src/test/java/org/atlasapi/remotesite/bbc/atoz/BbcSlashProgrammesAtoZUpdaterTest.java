package org.atlasapi.remotesite.bbc.atoz;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutorService;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.BbcSlashProgrammesRdfClient;
import org.atlasapi.remotesite.bbc.ChannelAndPid;
import org.atlasapi.remotesite.bbc.ProgressStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.io.Resources;
import com.google.common.util.concurrent.MoreExecutors;
import com.metabroadcast.common.http.FixedResponseHttpClient;

@RunWith(MockitoJUnitRunner.class)
public class BbcSlashProgrammesAtoZUpdaterTest {

    private static final String URI = "http://www.bbc.co.uk/radiofoyle/programmes/a-z/all.rdf";
    
    @SuppressWarnings("unchecked")
    private final SiteSpecificAdapter<Identified> fetcher = mock(SiteSpecificAdapter.class);
    private final ProgressStore progress = mock(ProgressStore.class);
    
    @Test
    public void testRunTask() {
        
        FixedResponseHttpClient client = FixedResponseHttpClient.respondTo(URI, Resources.getResource("radiofoyle-a-z-rdf.xml"));
        RemoteSiteClient<SlashProgrammesAtoZRdf> rdfClient = new BbcSlashProgrammesRdfClient<SlashProgrammesAtoZRdf>(client, SlashProgrammesAtoZRdf.class);
        ExecutorService executor = MoreExecutors.sameThreadExecutor();
        BbcSlashProgrammesAtoZUpdater updater = new BbcSlashProgrammesAtoZUpdater(rdfClient, executor, fetcher, progress);
        
        when(progress.getProgress()).thenReturn(new ChannelAndPid("radiofoyle", "b0070htg"));
        
        updater.run();
        
        verify(fetcher).fetch(BbcFeeds.slashProgrammesUriForPid("b00l8v40"));
        verify(progress).saveProgress("radiofoyle", "b00l8v40");
        verify(fetcher).fetch(BbcFeeds.slashProgrammesUriForPid("b00lg6w3"));
        verify(progress).saveProgress("radiofoyle", "b00lg6w3");
        verify(progress).resetProgress();
        
    }

}
