package org.atlasapi.remotesite.hulu;

import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.infoEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import javax.annotation.PreDestroy;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.html.HtmlNavigator;
import org.jdom.Element;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.scheduling.ScheduledTask;

public class HuluAllBrandsUpdater extends ScheduledTask {

    private static final String URL = "http://www.hulu.com/browse/alphabetical/episodes";
    
    private final HuluClient client;
    private final SiteSpecificAdapter<Brand> brandAdapter;
    private final AdapterLog log;
    private final ExecutorService executor;
    
    
    public HuluAllBrandsUpdater(HuluClient client, SiteSpecificAdapter<Brand> brandAdapter,  AdapterLog log) {
        this(client, brandAdapter, Executors.newFixedThreadPool(2), log);
    }

    public HuluAllBrandsUpdater(HuluClient client, SiteSpecificAdapter<Brand> brandAdapter, ExecutorService executor, AdapterLog log) {
        this.client = client;
        this.brandAdapter = brandAdapter;
		this.executor = executor;
        this.log = log;
    }

    @Override
    public void runTask() {
        try {
            log.record(infoEntry().withDescription("Retrieving all Hulu brands").withSource(getClass()));

            CompletionService<Void> completer = new ExecutorCompletionService<Void>(executor);
            Semaphore submitted = new Semaphore(0);//this could just be an integer but gives some idea what it's actually for.
            
            reportStatus("Submitting jobs");
            
            for (Runnable brandUpdateJob : getJobs()) {
                try {
                    completer.submit(brandUpdateJob, null);
                    submitted.release();
                } catch (Exception e) {
                    log.record(warnEntry().withCause(e).withDescription("Exception submitting Hulu brand update job").withSource(getClass()));
                }
            }
            
            int totalSubmitted = submitted.availablePermits();
            int processed = 0;
            int failures = 0;
            
            while(submitted.availablePermits() > 0) {
                reportStatus(String.format("Submitted %s jobs. %s processed. %s failed", totalSubmitted, processed, failures));
                Future<Void> completed = completer.take();
                submitted.acquire();
                try {
                    completed.get();
                    processed++;
                } catch (Exception e) {
                    failures++;
                }
            }
            
        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Error running update for All Hulu Brands"));
        }
    }

    public Iterable<Runnable> getJobs() throws HttpException, Exception {
        Maybe<HtmlNavigator> possibleNavigator = client.get(URL);
        if(possibleNavigator.hasValue()) {
            HtmlNavigator navigator = possibleNavigator.requireValue();
            return Iterables.filter(Iterables.transform(navigator.allElementsMatching("//div[@id='show_list_hiden']/a"), new Function<Element, Runnable>() {
                @Override
                public Runnable apply(Element element) {
                    String brandUri = element.getAttributeValue("href");
                    if (brandAdapter.canFetch(brandUri)) {
                        return new BrandFetchJob(brandUri);
                    }
                    return null;
                }
            }),Predicates.notNull());
        }
        return ImmutableList.of();
    }
    
    @PreDestroy
    public void destroy() {
        executor.shutdown();
    }

    class BrandFetchJob implements Runnable {

        private final String uri;

        public BrandFetchJob(String uri) {
            this.uri = uri;
        }

        public void run() {
            try {
                brandAdapter.fetch(uri);
            } catch (Exception e) {
                log.record(AdapterLogEntry.errorEntry().withDescription("Error retrieving Hulu brand %s",uri).withCause(e).withSource(getClass()));
            }
        }
    }
}
