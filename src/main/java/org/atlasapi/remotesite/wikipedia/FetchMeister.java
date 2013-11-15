package org.atlasapi.remotesite.wikipedia;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final int lookAheadArticles = 20;

    /**
     * Thrown when the next article is requested but there are no more.
     */
    public static class NoMoreArticlesException extends Exception {};

    /**
     * Special value to put on the preloadedBaseArticles queue, indicating there's no more articles to load.
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

    private final Iterator<String> titles;
    private final Thread fetchingThread;

    private boolean shouldEndSelf = false;

    private static class Request {
        public String title;
        public SettableFuture<Article> future;

        public Request(String title, SettableFuture<Article> future) {
            this.title = title;
            this.future = future;
        }
    }
    private final BlockingQueue<Request> childArticleRequests = new LinkedBlockingQueue<Request>();

    private final BlockingQueue<Article> preloadedBaseArticles = new ArrayBlockingQueue<Article>(lookAheadArticles);

    public FetchMeister(final ArticleFetcher fetcher, Iterable<String> titleSource) {
        this.titles = titleSource.iterator();

        fetchingThread = new Thread() {
            public void run() {
                while (!shouldEndSelf) {
                    // do we have anything to do at all?
                    synchronized (fetchingThread) {
                        while (!shouldEndSelf && childArticleRequests.isEmpty() && preloadedBaseArticles.remainingCapacity() == 0) {
                            try { fetchingThread.wait(); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                        }
                    }

                    // try first to service any child article request that might be waiting
                    Request r = childArticleRequests.poll();
                    if (r != null) {
                        log.debug("Fetching child article \""+ r.title +"\"");
                        r.future.set(fetcher.fetchArticle(r.title));
                    } else {
                      // otherwise, if no child articles are waiting to be fetched...
                        // preload another base article if we want one
                        if (preloadedBaseArticles.remainingCapacity() > 0) {
                            if (titles.hasNext()) {
                                String title = titles.next();
                                log.debug("Prefetching \""+ title +"\"...");
                                preloadedBaseArticles.offer(fetcher.fetchArticle(title));
                            } else {
                                preloadedBaseArticles.offer(NO_MORE_ARTICLES);
                            }
                        }
                    }
                }
            }
        };
    }

    /**
     * Returns the next article from the prespecified list of titles.
     * <p>
     * Note: even though it returns a Future, this is somewhat pointless as it currently just blocks while loading hasn't finished.
     */
    public ListenableFuture<Article> fetchNextBaseArticle() throws NoMoreArticlesException {
        while (true) {
            try {
                Article a = preloadedBaseArticles.take();  // blocks if we don't have one yet
                synchronized (fetchingThread) { fetchingThread.notify(); }
                if(a == NO_MORE_ARTICLES) { throw new NoMoreArticlesException(); }
                log.info("TV brand article \""+ a.getTitle() +"\" was fetched for processing");
                return Futures.immediateFuture(a);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
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

    public void start() {
        fetchingThread.start();
    }

    public void stop() {
        shouldEndSelf = true;
        synchronized(fetchingThread) { fetchingThread.notify(); }
    }
}
