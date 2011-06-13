package org.atlasapi.remotesite.bbc.atoz;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.bbc.BbcProgrammeAdapter;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class BbcSlashProgrammesAtoZUpdater extends ScheduledTask {
	
    private static final String ATOZ_BASE = "http://www.bbc.co.uk/%s/programmes/a-z/all.rdf";
    private static final String SLASH_PROGRAMMES_BASE_URI = "http://www.bbc.co.uk/programmes/";

    private final RemoteSiteClient<SlashProgrammesAtoZRdf> client;
    private final BbcProgrammeAdapter fetcher;
    private final List<String> channels = ImmutableList.of("radio2", "radio1", "radio3", "radio4", "bbcone", "bbctwo", "bbcthree", "bbcfour", "bbchd");
	
	private final AdapterLog log;
    
    public BbcSlashProgrammesAtoZUpdater(ContentWriter writer, AdapterLog log) {
        this(new BbcSlashProgrammesAtoZRdfClient(), new BbcProgrammeAdapter(writer, log), log);
    }

    public BbcSlashProgrammesAtoZUpdater(RemoteSiteClient<SlashProgrammesAtoZRdf> client, BbcProgrammeAdapter fetcher, AdapterLog log) {
        this.client = client;
        this.fetcher = fetcher;
		this.log = log;
    }

    @Override
    public void runTask() {
    	ExecutorService executor = Executors.newFixedThreadPool(3);
        for (String channel : channels) {
            final String uri = String.format(ATOZ_BASE, channel);
            try {
                SlashProgrammesAtoZRdf atoz = client.get(uri);
                for (final String pid : atoz.programmeIds()) {
                    executor.execute(new Runnable() {
						@Override
						public void run() {
							if (!shouldContinue()) {
								return;
							}
							reportStatus(uri + " : " + pid);
							loadAndSave(pid);							
						}
                    });
                }
            } catch (Exception e) {
            	log.record(new AdapterLogEntry(Severity.WARN).withCause(e).withUri(uri).withDescription("Failed to load BBC atoz page: " + uri).withSource(getClass()));
            }
        }
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.DAYS);
        }catch (InterruptedException e) {
            log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withCause(e).withDescription("Interrupted waiting for executor to terminate"));
        }
    }

	private void loadAndSave(String pid) {
		String uri = SLASH_PROGRAMMES_BASE_URI + pid;
		try {
			fetcher.createOrUpdate(uri);
		} catch (Exception e) {
        	log.record(new AdapterLogEntry(Severity.WARN).withCause(e).withUri(uri).withSource(getClass()));
		}
	}
}
