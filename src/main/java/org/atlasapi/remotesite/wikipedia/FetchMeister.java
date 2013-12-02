package org.atlasapi.remotesite.wikipedia;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

/**
 * A helper class that tries to smooth the process of fetching pages from Wikipedia.
 * <p>
 * Rather than using an {@link ArticleFetcher} directly, updaters may go through this class. Benefits include:
 * <ul>
 *   <li>The certainty that only one article is being fetched from Wikipedia at a time. (i.e. throttling)</li>
 *   <li>Automatic preloading of articles from the prespecified source of titles (during the time when no specific articles have been requested for immediate loading), so they can hopefully be available more quickly more often.</li>
 * </ul>
 */
public class FetchMeister {
    private static final Logger log = LoggerFactory.getLogger(FetchMeister.class);

    /**
     * The maximum number of articles to preload from the title source.
     */
    private static final int MAX_PRELOAD = 20;

    /**
     * Special poison value to put on the PreloadedArticlesQueues' BlockingQueue, indicating there's no more titles left to load.
     * ({@link BlockingQueue} has no other mechanism to indicate this.)
     */
    private static final Article NO_MORE_ARTICLES = new Article() {
        public DateTime getLastModified() {
            throw new UnsupportedOperationException("Not a real article.");
        }
        public String getMediaWikiSource() {
            throw new UnsupportedOperationException("Not a real article.");
        }
        public String getTitle() {
            return "NO_MORE_ARTICLES";
        }
    };

    /**
     * A representation of a queue of articles, to be handed out to things that want to preload from a list of titles. 
     */
    public static class PreloadedArticlesQueue {
        /**
         * Thrown when the next article is requested but there are no more.
         */
        public static class NoMoreArticlesException extends Exception {};
        
        private final FetchMeister fetcher;
        private final Iterator<String> titles;
        private final BlockingQueue<Article> preloadedArticles = new ArrayBlockingQueue<Article>(MAX_PRELOAD);
        private boolean finished = false;
        
        private PreloadedArticlesQueue(FetchMeister fetcher, Iterator<String> titles) {
            this.fetcher = fetcher;
            this.titles = titles;
        }
        
        /**
         * Returns the next article from the prespecified list of titles.
         * <p>
         * Note: even though it returns a Future, this is somewhat pointless as it currently just blocks while loading hasn't finished.
         */
        public ListenableFuture<Article> fetchNextBaseArticle() throws NoMoreArticlesException {
            if (finished) {
                throw new NoMoreArticlesException();
            }
            while (true) {
                try {
                    Article a = preloadedArticles.take();  // blocks if we don't have one yet
                    synchronized (fetcher.fetchingThread) { fetcher.fetchingThread.notify(); }  // notify in case we just created space to fetch more
                    if(a == NO_MORE_ARTICLES) {
                        finished = true;
                        throw new NoMoreArticlesException();
                    }
                    log.info("Base article \""+ a.getTitle() +"\" was fetched for processing");
                    return Futures.immediateFuture(a);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    private final ArticleFetcher fetcher;
    private FetchingThread fetchingThread;

    private volatile int users = 0;
    
    private List<PreloadedArticlesQueue> preloaders = new LinkedList<>();

    private static class Request {
        public String title;
        public SettableFuture<Article> future;

        public Request(String title, SettableFuture<Article> future) {
            this.title = title;
            this.future = future;
        }
    }
    private final BlockingQueue<Request> childArticleRequests = new LinkedBlockingQueue<Request>();
    
    public FetchMeister(final ArticleFetcher fetcher) {
        this.fetcher = fetcher;
    }

    private PreloadedArticlesQueue lastQueue = null;  // state: the last queue that was served
    
    /**
     * Returns the next {@link PreloadedArticlesQueue} that can be served, unless there isn't one.
     */
    private Optional<PreloadedArticlesQueue> nextQueue() {
        synchronized (preloaders) {
            if (preloaders.isEmpty()) { return Optional.absent(); }
            
            boolean lastQueueHasBeenSeen = false;
            while (true) {
                for (PreloadedArticlesQueue q : preloaders) {
                    if (lastQueue == null) { lastQueue = q; lastQueueHasBeenSeen = true; continue; }
                    if (lastQueueHasBeenSeen) {
                        if (q.preloadedArticles.remainingCapacity() > 0) { return Optional.of(q); }
                        if (q == lastQueue) { return Optional.absent(); }
                    }
                    if (q == lastQueue) { lastQueueHasBeenSeen = true; }
                }
            }
        }
    }
    
    private class FetchingThread extends Thread {
        volatile boolean shouldEndSelf = false;
        
        public void run() {
            while (!shouldEndSelf) {
                boolean didNothingThisTime = true;
                
                // try first to service any child article request that might be waiting
                Request r = childArticleRequests.poll();
                if (r != null) {
                    didNothingThisTime = false;
                    log.debug("Fetching child article \""+ r.title +"\"");
                    r.future.set(fetcher.fetchArticle(r.title));
                } else {
                  // otherwise, if no child articles are waiting to be fetched...
                    // preload another base article if we want one
                    PreloadedArticlesQueue queue = nextQueue().orNull();
                    if (queue != null) {
                        didNothingThisTime = false;
                        lastQueue = queue;
                        if (queue.titles.hasNext()) {
                            String title = queue.titles.next();
                            log.debug("Prefetching \""+ title +"\"...");
                            queue.preloadedArticles.offer(fetcher.fetchArticle(title));
                        } else {
                            queue.preloadedArticles.offer(NO_MORE_ARTICLES);
                            cancelPreloading(queue);
                        }
                    }
                }
                
                if(didNothingThisTime) {  // do we have anything to do at all..? wait while we don't.
                    synchronized (this) {
                        while (!shouldEndSelf && childArticleRequests.isEmpty() && !(nextQueue().isPresent())) {
                            try { wait(); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Queues up a specified article for priority fetching (i.e. prioritized above preloading more from the TitleSource). Returns a Future of that article.
     */
    public ListenableFuture<Article> fetchChildArticle(String title) {
        SettableFuture<Article> f = SettableFuture.create();
        childArticleRequests.add(new Request(title, f));
        synchronized (fetchingThread) { fetchingThread.notify(); }
        return f;
    }
    
    public PreloadedArticlesQueue queueForPreloading(Iterable<String> titleSource) {
        synchronized (preloaders) {
            PreloadedArticlesQueue queue = new PreloadedArticlesQueue(this, titleSource.iterator());
            preloaders.add(queue);
            synchronized (fetchingThread) { fetchingThread.notify(); }
            return queue;
        }
    }
    
    public void cancelPreloading(PreloadedArticlesQueue queue) {
        synchronized (preloaders) {
            if (lastQueue == queue) { lastQueue = null; }
            preloaders.remove(queue);
        }
    }

    public synchronized void start() {
        if (fetchingThread == null) {
            fetchingThread = new FetchingThread();
            fetchingThread.start();
        }
        ++users;
    }

    public synchronized void stop() {
        --users;
        if (users == 0) {
            fetchingThread.shouldEndSelf = true;
            synchronized(fetchingThread) { fetchingThread.notify(); }
            fetchingThread = null;
        }
    }
}
