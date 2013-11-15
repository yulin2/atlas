package org.atlasapi.remotesite.wikipedia;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.remotesite.wikipedia.television.BrandInfoboxScraper;
import org.atlasapi.remotesite.wikipedia.television.EpisodeListScraper;
import org.atlasapi.remotesite.wikipedia.television.ScrapedFlatHierarchy;
import org.atlasapi.remotesite.wikipedia.television.SeasonSectionScraper;
import org.atlasapi.remotesite.wikipedia.television.TvBrandArticleTitleSource;
import org.atlasapi.remotesite.wikipedia.television.TvBrandHierarchy;
import org.atlasapi.remotesite.wikipedia.television.TvBrandHierarchyExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sweble.wikitext.lazy.preprocessor.LazyPreprocessedPage;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.metabroadcast.common.scheduling.ScheduledTask;

public final class TvBrandHierarchyUpdater extends ScheduledTask {
    private static final Logger log = LoggerFactory.getLogger(TvBrandHierarchyUpdater.class);
    
    /** How many brands we try to process at once. */
    private static final int SIMULTANEOUSNESS = 4;
    /** How many threads we use for all the deferred processing. */
    private static final int THREADS = 4;
    
    private ListeningExecutorService executor;
    private final FetchMeister fetchMeister;
    private final CountDownLatch countdown = new CountDownLatch(SIMULTANEOUSNESS);
    
    private final TvBrandHierarchyExtractor extractor;
    private final ContentWriter writer;
    
    public TvBrandHierarchyUpdater(TvBrandArticleTitleSource titleSource, ArticleFetcher fetcher, TvBrandHierarchyExtractor extractor, ContentWriter writer) {
        fetchMeister = new FetchMeister(Preconditions.checkNotNull(fetcher), titleSource.getAllTvBrandArticleTitles());
        this.extractor = Preconditions.checkNotNull(extractor);
        this.writer = Preconditions.checkNotNull(writer);
    }
    
    /**
     * Goes and iterates over all the given titles.
     */
    @Override
    public void runTask() {
        fetchMeister.start();
        executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(THREADS));
        for(int i=0; i<SIMULTANEOUSNESS; ++i) {
            processNext();
        }
        while(true) {
            try {
                countdown.await();
                break;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        executor.shutdown();
        fetchMeister.stop();
    }
    
    private void processNext() {
        ListenableFuture<Article> next;
        try {
            if(!shouldContinue()) {
                log.info("TvBrandHierarchyUpdater has been cancelled and is stopping.");
                throw new FetchMeister.NoMoreArticlesException();
            }
            next = fetchMeister.fetchNextBaseArticle();
        } catch (FetchMeister.NoMoreArticlesException ex) {
            countdown.countDown();
            return;
        }
        Futures.addCallback(updateBrand(next), new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                processNext();
            }
            @Override
            public void onFailure(Throwable t) {
                log.warn("Failed to process a TV brand", t);
                processNext();
            }
        });
    }
    
    /**
     * A useful function to feed to {@link Futures#transform} -- converts a bunch of immutable lists of {@link EpisodeListScraper#Result} into one long list, by concatting them.
     */
    private static final Function<List<ImmutableCollection<EpisodeListScraper.Result>>, ImmutableCollection<EpisodeListScraper.Result>> CONCAT = new Function<List<ImmutableCollection<EpisodeListScraper.Result>>, ImmutableCollection<EpisodeListScraper.Result>>() {
        public ImmutableCollection<EpisodeListScraper.Result> apply(List<ImmutableCollection<EpisodeListScraper.Result>> input) {
            ImmutableList.Builder<EpisodeListScraper.Result> b = ImmutableList.builder();
            for(ImmutableCollection<EpisodeListScraper.Result> c : input) {
                if(c != null) {
                    b.addAll(c);
                }
            }
            return b.build();
        }
    };
    
    /**
     * Scrapes out all the episodes listed in {@code epList}, making sure each knows (where possible and appropriate) the season from which it came.
     */
    private ListenableFuture<ImmutableCollection<EpisodeListScraper.Result>> extractSeasons(ListenableFuture<Article> epList) {
        return Futures.transform(epList, new AsyncFunction<Article, ImmutableCollection<EpisodeListScraper.Result>>() {
                public ListenableFuture<ImmutableCollection<EpisodeListScraper.Result>> apply(final Article epList) {
                    final ImmutableList.Builder<ListenableFuture<ImmutableCollection<EpisodeListScraper.Result>>> builder = ImmutableList.builder();
                    log.debug("Commencing seasonSectionScraping on \""+ epList.getTitle() +"\"");
                    
                    final Boolean[] foundASection = {false};
                    new SeasonSectionScraper(new Callback<SeasonSectionScraper.Result>() {
                        public void have(final SeasonSectionScraper.Result season) {
                            foundASection[0] = true;
                            log.debug("Extracting on "+epList.getTitle()+" "+season.name);
                            builder.add(extractFlatEpisodes(season.content, season, false, season.name));
                        }
                    }).go(SwebleHelper.parse(epList.getMediaWikiSource()));
                    
                    if(foundASection[0]){  // then this is a page with sections and everything worked properly
                        return Futures.transform(Futures.successfulAsList(builder.build()), CONCAT);
                    } else {  // otherwise, there were no sections – we should just scan for episodes, leaving out the concept of seasons
                        return extractFlatEpisodes(epList.getMediaWikiSource(), null, false, epList.getTitle());
                    }
                }
            }, executor);
    }

    /**
     * Scrapes out episode records by preprocessing the given text.
     * @param source The text to parse.
     * @param season A season result to inject into the episodes.
     * @param includeOnly Is this being called on an article that was included as a template? If so, we choose to only parse the {@code includeOnly} section of the source.
     * @param context For logging/debug purposes, some explanation of the text on which this is being called.
     */
    private ListenableFuture<ImmutableCollection<EpisodeListScraper.Result>> extractFlatEpisodes(String source, final SeasonSectionScraper.Result season, boolean includeOnly, final String context) {
        final ImmutableList.Builder<ListenableFuture<ImmutableCollection<EpisodeListScraper.Result>>> laters = ImmutableList.builder();
        final ImmutableList.Builder<EpisodeListScraper.Result> immediates = ImmutableList.builder();

        LazyPreprocessedPage section = SwebleHelper.preprocess(source, includeOnly);
        new EpisodeListScraper(new Callback<EpisodeListScraper.Result>() {
            public void have(EpisodeListScraper.Result result) {
                immediates.add(result);
            }
        }, new Callback<String>() {
            public void have(String includeTitle) {
                log.debug("Queueing fetch of \""+ includeTitle +"\" as child list for " + context);
                laters.add(Futures.transform(fetchMeister.fetchChildArticle(includeTitle), new AsyncFunction<Article, ImmutableCollection<EpisodeListScraper.Result>>() {
                    public ListenableFuture<ImmutableCollection<EpisodeListScraper.Result>> apply(Article childArticle) {
                        return extractFlatEpisodes(childArticle.getMediaWikiSource(), season, true, childArticle.getTitle());
                    }
                }, executor));
            }
        }).withSeason(season).go(section);
        ImmutableCollection<EpisodeListScraper.Result> immeds = immediates.build();
        laters.add(Futures.immediateFuture(immeds));
        return Futures.transform(Futures.successfulAsList(laters.build()), CONCAT);
    }
    
    /**
     * Does all subfetching/resolution/writing for a given TV brand article.
     */
    private ListenableFuture<Void> updateBrand(final ListenableFuture<Article> baseArticle) {

        ListenableFuture<BrandInfoboxScraper.Result> brandInfobox = Futures.transform(baseArticle, new Function<Article,BrandInfoboxScraper.Result>() {
            public BrandInfoboxScraper.Result apply(final Article baseArticle) {
                LazyPreprocessedPage preprocess = SwebleHelper.preprocess(baseArticle.getMediaWikiSource(), true);
                BrandInfoboxScraper.Result res;
                final LinkedList<BrandInfoboxScraper.Result> it = Lists.newLinkedList();
                new BrandInfoboxScraper(new Callback<BrandInfoboxScraper.Result>() {
                    public void have(BrandInfoboxScraper.Result thing) {
                        it.add(thing);
                    }
                }).go(preprocess);
                return it.getFirst();
            }
        }, executor);

        ListenableFuture<ImmutableCollection<EpisodeListScraper.Result>> externalListedEpisodes;
        externalListedEpisodes = Futures.transform(brandInfobox, new AsyncFunction<BrandInfoboxScraper.Result, ImmutableCollection<EpisodeListScraper.Result>>() {
            public ListenableFuture<ImmutableCollection<EpisodeListScraper.Result>> apply(BrandInfoboxScraper.Result input) {
                if(input.episodeListLinkTarget == null || input.episodeListLinkTarget.isEmpty()) {
                    ImmutableCollection<EpisodeListScraper.Result> none = ImmutableList.of();
                    return Futures.immediateFuture(none);
                }
                log.debug("Queueing fetch of \""+ input.episodeListLinkTarget +"\" as season list for \""+ input.title +"\"");
                return extractSeasons(fetchMeister.fetchChildArticle(input.episodeListLinkTarget));
            }
        }, executor);

        ListenableFuture<ImmutableCollection<EpisodeListScraper.Result>> mainPageEpisodes = extractSeasons(baseArticle);
        
        ListenableFuture<ImmutableCollection<EpisodeListScraper.Result>> allEpisodes = Futures.transform(Futures.successfulAsList(mainPageEpisodes, externalListedEpisodes), CONCAT);
        
        ListenableFuture<Void> doIt = Futures.transform(new ScrapedFlatHierarchy.Collector(baseArticle, brandInfobox, allEpisodes).collect(), new Function<ScrapedFlatHierarchy, Void>() {
            public Void apply(ScrapedFlatHierarchy input) {
                TvBrandHierarchy extracted = extractor.extract(input);
                writeHierarchy(extracted);
                return null;
            }
        }, executor);
        
        return doIt;
    }
    
    private void writeHierarchy(TvBrandHierarchy hierarchy) {
        writer.createOrUpdate(hierarchy.getBrand());
        for (Series s : hierarchy.getSeasons()) {
            writer.createOrUpdate(s);
        }
        for (Episode e : hierarchy.getEpisodes()) {
            writer.createOrUpdate(e);
        }
    }

}
