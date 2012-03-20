package org.atlasapi.remotesite.bbc.atoz;

import org.atlasapi.media.content.Identified;
import org.atlasapi.persistence.logging.NullAdapterLog;
import org.atlasapi.remotesite.FixedResponseHttpClient;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.BbcSlashProgrammesRdfClient;
import org.atlasapi.remotesite.bbc.ProgressStore;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.MoreExecutors;

@RunWith(JMock.class)
public class BbcSlashProgrammesAtoZUpdaterTest {

    private static final String URI = "http://www.bbc.co.uk/radiofoyle/programmes/a-z/all.rdf";
    
    private final Mockery context = new Mockery();
    private final ProgressStore progress = context.mock(ProgressStore.class);
    private final @SuppressWarnings("unchecked") SiteSpecificAdapter<Identified> fetcher = context.mock(SiteSpecificAdapter.class);
    
    @Test
    public void testRunTask() {
        
        FixedResponseHttpClient client = FixedResponseHttpClient.respondTo(URI, Resources.getResource("radiofoyle-a-z-rdf.xml"));
        BbcSlashProgrammesRdfClient<SlashProgrammesAtoZRdf> rdfClient = new BbcSlashProgrammesRdfClient<SlashProgrammesAtoZRdf>(client, SlashProgrammesAtoZRdf.class);
        BbcSlashProgrammesAtoZUpdater updater = new BbcSlashProgrammesAtoZUpdater(rdfClient, fetcher, progress, new NullAdapterLog(), MoreExecutors.sameThreadExecutor());
        
        context.checking(new Expectations(){{
            one(progress).getProgress();will(returnValue(Maps.immutableEntry("radiofoyle", "b0070htg")));
            one(fetcher).fetch(BbcFeeds.slashProgrammesUriForPid("b00l8v40"));
            one(progress).saveProgress("radiofoyle", "b00l8v40");
            one(fetcher).fetch(BbcFeeds.slashProgrammesUriForPid("b00lg6w3"));
            one(progress).saveProgress("radiofoyle", "b00lg6w3");
        }});
        
        updater.run();
    }

}
