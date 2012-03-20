package org.atlasapi.remotesite.bbc.atoz;

import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.atlasapi.media.content.Identified;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.BbcProgrammeAdapter;
import org.atlasapi.remotesite.bbc.ProgressStore;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class BbcSlashProgrammesAtoZUpdater extends ScheduledTask {
	
    private static final String ATOZ_BASE = "http://www.bbc.co.uk/%s/programmes/a-z/all.rdf";

    private final List<String> channels = ImmutableList.of(
        "bbcone", "bbctwo", "bbcthree", "bbcfour", "bbchd", 
        "radio1", "radio2", "radio3", "radio4", "radio4extra", "5live", "5livesportsextra", "6music", "asiannetwork", 
        "radioscotland", "radionangaidheal", "radiowales", "radiocymru", "radioulster", "radiofoyle"
    );

    private final RemoteSiteClient<SlashProgrammesAtoZRdf> client;
    private final SiteSpecificAdapter<Identified> fetcher;

	private final AdapterLog log;
    private final ProgressStore progressStore;
    private final ExecutorService executor;

    public BbcSlashProgrammesAtoZUpdater(ProgressStore progressStore, BbcProgrammeAdapter programmeAdapter, AdapterLog log) {
        this(new BbcSlashProgrammesAtoZRdfClient(), programmeAdapter, progressStore, log, null);
    }

    public BbcSlashProgrammesAtoZUpdater(RemoteSiteClient<SlashProgrammesAtoZRdf> client, SiteSpecificAdapter<Identified> fetcher, ProgressStore progressStore, AdapterLog log, ExecutorService executor) {
        this.client = client;
        this.fetcher = fetcher;
        this.progressStore = progressStore;
		this.log = log;
        this.executor = executor;
    }

    @Override
    public void runTask() {
    	ExecutorService executor = this.executor == null ? Executors.newFixedThreadPool(3) : this.executor;
    	
    	Entry<String, String> restoredProgress = progressStore.getProgress();
    	List<String> remainingChannels = remainingChannels(restoredProgress);
    	String lastPid = lastCompletedPid(restoredProgress);
    	
        for (final String channelKey : remainingChannels) {
            final String channelAzUri = String.format(ATOZ_BASE, channelKey);
            try {
                SlashProgrammesAtoZRdf atoz = client.get(channelAzUri);
                ImmutableList<String> sortedPids = Ordering.natural().immutableSortedCopy(atoz.programmeIds());
                for (final String pid : sortedPids) {
                    if(lastPid != null && pid.compareTo(lastPid) <= 0) {
                        continue;//fast-forward.
                    }
                    executor.execute(taskForPid(pid, channelKey));
                    if(!shouldContinue()) {
                        break;
                    }
                }
            } catch (Exception e) {
            	log.record(warnEntry().withCause(e).withUri(channelAzUri).withDescription("Failed to load BBC atoz page: " + channelAzUri).withSource(getClass()));
            	Throwables.propagate(e);
            }
            lastPid = null;
            if(!shouldContinue()) {
                break;
            }
        }
        
        shutdown(executor);
    }

    private String lastCompletedPid(Entry<String, String> progress) {
        return progress == null ? null : progress.getValue();
    }

    private List<String> remainingChannels(Entry<String, String> progress) {
        return progress == null ? channels : channels.subList(channels.indexOf(progress.getKey()), channels.size());
    }

    private void shutdown(ExecutorService executor) {
        executor.shutdown();
        try {
            reportStatus("Awaiting shutdown");
            executor.awaitTermination(10, TimeUnit.DAYS);
        }catch (InterruptedException e) {
            log.record(new AdapterLogEntry(Severity.INFO).withSource(getClass()).withCause(e).withDescription("Interrupted waiting for executor to terminate"));
        }
    }

    private Runnable taskForPid(final String pid, final String channelKey) {
        return new Runnable() {
        	@Override
        	public void run() {
        		if (!shouldContinue()) {
        			return;
        		}
        		reportStatus(channelKey + " : " + pid);
        		loadAndSave(pid);		
        		progressStore.saveProgress(channelKey, pid);
        	}
        };
    }

    private void loadAndSave(String pid) {
        String uri = BbcFeeds.slashProgrammesUriForPid(pid);
        try {
            fetcher.fetch(uri);
        } catch (Exception e) {
            log.record(AdapterLogEntry.warnEntry().withCause(e).withUri(uri).withSource(getClass()));
        }
    }
}
