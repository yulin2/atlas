package org.atlasapi.remotesite.bbc.atoz;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.query.uri.SavingFetcher;

import com.google.common.collect.ImmutableList;

public class BbcSlashProgrammesAtoZUpdater implements Runnable {
    private static final String ATOZ_BASE = "http://www.bbc.co.uk/%s/programmes/a-z/all.rdf";
    private static final String SLASH_PROGRAMMES_BASE_URI = "http://www.bbc.co.uk/programmes/";

    private static Log log = LogFactory.getLog(BbcSlashProgrammesAtoZUpdater.class);

    private final RemoteSiteClient<SlashProgrammesAtoZRdf> client;
    private final SavingFetcher fetcher;
    private final List<String> channels = ImmutableList.of("bbcone", "bbctwo", "bbcthree", "bbcfour", "bbchd", "radio1", "radio2", "radio3", "radio4");
    
    public BbcSlashProgrammesAtoZUpdater(SavingFetcher fetcher) {
        this(new BbcSlashProgrammesAtoZRdfClient(), fetcher);
    }

    public BbcSlashProgrammesAtoZUpdater(RemoteSiteClient<SlashProgrammesAtoZRdf> client, SavingFetcher fetcher) {
        this.client = client;
        this.fetcher = fetcher;
    }

    @Override
    public void run() {
        for (String channel : channels) {
            String uri = String.format(ATOZ_BASE, channel);

            try {
                SlashProgrammesAtoZRdf atoz = client.get(uri);
                for (String pid : atoz.programmeIds()) {
                    try {
                        fetcher.fetch(SLASH_PROGRAMMES_BASE_URI + pid);
                    } catch (Exception e) {
                        log.warn("Error fetching results for bbc programme " + pid, e);
                    }
                }
            } catch (Exception e) {
                log.warn("Problem fetching the atoz for channel: " + channel, e);
            }
        }
    }
}
