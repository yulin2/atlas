package org.atlasapi.remotesite.bbc.atoz;

import static com.metabroadcast.common.scheduling.UpdateProgress.FAILURE;
import static com.metabroadcast.common.scheduling.UpdateProgress.SUCCESS;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.BbcSlashProgrammesPidSource;
import org.atlasapi.remotesite.bbc.ChannelAndPid;
import org.atlasapi.remotesite.bbc.ProgressStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.metabroadcast.common.scheduling.ScheduledTask;
import com.metabroadcast.common.scheduling.UpdateProgress;

public class BbcSlashProgrammesAtoZUpdater extends ScheduledTask {
    
    private static final Logger log = LoggerFactory.getLogger(BbcSlashProgrammesAtoZUpdater.class);

    private static final List<String> CHANNELS = ImmutableList.of(
        "bbcone", "bbctwo", "bbcthree", "bbcfour", "bbchd", 
        "radio1", "radio2", "radio3", "radio4", "radio4extra", "5live", "5livesportsextra", "6music", "asiannetwork", 
        "radioscotland", "radionangaidheal", "radiowales", "radiocymru", "radioulster", "radiofoyle"
    );
    
    private final BbcSlashProgrammesPidSource pidSource;
    private final ListeningExecutorService executor;
    private final SiteSpecificAdapter<? extends Identified> fetcher;
    private final ProgressStore progressStore;

    private final AtomicReference<ChannelAndPid> lastSaved = new AtomicReference<ChannelAndPid>();

    public BbcSlashProgrammesAtoZUpdater(RemoteSiteClient<SlashProgrammesAtoZRdf> client, ExecutorService executor, SiteSpecificAdapter<? extends Identified> programmeAdapter, ProgressStore progressStore) {
        this.pidSource = new BbcSlashProgrammesPidSource(client, CHANNELS);
        this.fetcher = programmeAdapter;
        this.progressStore = progressStore;
        this.executor = MoreExecutors.listeningDecorator(executor);
    }

    @Override
    public void runTask() {
    	
    	ChannelAndPid restoredProgress = progressStore.getProgress();
    	lastSaved.set(restoredProgress);
    	if (restoredProgress != null) {
    	    log.info("continuing from {}", restoredProgress);
    	}
    	
    	AtomicReference<UpdateProgress> progress = new AtomicReference<UpdateProgress>(UpdateProgress.START);
    	Semaphore submitted = new Semaphore(5);
    	
    	try {
        	Iterator<ChannelAndPid> caps = pidSource.listPids(restoredProgress).iterator();
        	while(shouldContinue() && caps.hasNext()) {
        	    ChannelAndPid cap = caps.next();
                submitted.acquire();
                log.debug("submitted " + cap);
                ListenableFuture<?> result = executor.submit(taskFor(cap));
                Futures.addCallback(result,updateProgressCallback(cap, progress, submitted));
            }
        	submitted.acquire(5);
    	} catch (InterruptedException e) {
    	    Throwables.propagate(e);
    	}
    	
        if(shouldContinue()) {
            progressStore.resetProgress();
        }
    }

    private FutureCallback<Object> updateProgressCallback(final ChannelAndPid cap, final AtomicReference<UpdateProgress> progress, final Semaphore submitted) {
        return new FutureCallback<Object>() {

            @Override
            public void onSuccess(Object result) {
                if (shouldContinue()) {
                    saveProgress(cap);
                }
                updateProgress(SUCCESS);
            }

            private void updateProgress(UpdateProgress update) {
                submitted.release();
                progress.set(progress.get().reduce(update));
                reportStatus(progress.get().toString());
            }

            @Override
            public void onFailure(Throwable t) {
                updateProgress(FAILURE);
            }
        };
    }

    private Runnable taskFor(final ChannelAndPid cap) {
        return new Runnable() {
            @Override
            public void run() {
                if (shouldContinue()) {
                    String uri = BbcFeeds.slashProgrammesUriForPid(cap.pid());
                    fetcher.fetch(uri);
                    log.debug("fetched " + uri);
                }
            }
        };
    }
    
    private void saveProgress(ChannelAndPid cap) {
        synchronized (lastSaved) {
            ChannelAndPid prevCap = lastSaved.get();
            if (prevCap == null || laterChannel(cap, prevCap) || laterPid(cap, prevCap)){
                progressStore.saveProgress(cap.channel(), cap.pid());
                lastSaved.set(cap);
            }
        }
    }

    private boolean laterPid(ChannelAndPid cap, ChannelAndPid prevCap) {
        return cap.channel().equals(prevCap.channel()) 
            && cap.pid().compareTo(prevCap.pid()) > 0;
    }

    private boolean laterChannel(ChannelAndPid cap, ChannelAndPid prevCap) {
        return CHANNELS.indexOf(cap.channel())>CHANNELS.indexOf(prevCap.channel());
    }

   
}
