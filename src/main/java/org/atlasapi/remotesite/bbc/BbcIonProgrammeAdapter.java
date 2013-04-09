package org.atlasapi.remotesite.bbc;

import static org.atlasapi.media.entity.Publisher.BBC;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.ContentStore;
import org.atlasapi.media.entity.Alias;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Version;
import org.atlasapi.media.segment.SegmentEvent;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesBase;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesClip;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesSeriesContainer;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesSeriesRef;
import org.atlasapi.remotesite.bbc.ion.BbcExtendedDataContentAdapter;
import org.atlasapi.remotesite.bbc.ion.BbcIonItemMerger;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class BbcIonProgrammeAdapter extends AbstractBbcAdapter<Content> {

    private final ContentStore store;
    
    private final SiteSpecificAdapter<Item> episodeAdapter;
    private final SiteSpecificAdapter<List<SegmentEvent>> segmentAdapter;
    private final BbcIonItemMerger merger;

    private final BbcExtendedDataContentAdapter extendedDataAdapter;
    
    private final RemoteSiteClient<SlashProgrammesRdf> slashProgrammesClient;
    private final SiteSpecificAdapter<Container> containerAdapter;
    private final SiteSpecificAdapter<Clip> clipAdapter;
    private final ListeningExecutorService executor;

    public BbcIonProgrammeAdapter(ContentStore store, SiteSpecificAdapter<Item> episodeAdapter, BbcExtendedDataContentAdapter extendedDataAdapter, SiteSpecificAdapter<List<SegmentEvent>> segmentAdapter, RemoteSiteClient<SlashProgrammesRdf> slashProgrammesClient, SiteSpecificAdapter<Container> containerAdapter, SiteSpecificAdapter<Clip> clipAdapter, ExecutorService executor) {
        this.store = store;
        this.episodeAdapter = episodeAdapter;
        this.extendedDataAdapter = extendedDataAdapter;
        this.segmentAdapter = segmentAdapter;
        this.slashProgrammesClient = slashProgrammesClient;
        this.containerAdapter = containerAdapter;
        this.clipAdapter = clipAdapter;
        this.executor = MoreExecutors.listeningDecorator(executor);
        this.merger = new BbcIonItemMerger();
    }
    
    @Override
    public Content fetch(String uri) {
        Preconditions.checkArgument(canFetch(uri), "Can't fetch " + uri);
        Item fetchedItem = fetchItem(uri, null);
        if (fetchedItem != null) {
            return fetchedItem;
        }
        return fetchContainer(uri);
    }

    protected Container fetchContainer(String uri) {
        Container fetchedContainer = containerAdapter.fetch(uri);
        if (fetchedContainer == null) {
            return null;
        }
        addExtendedData(fetchedContainer, extendedDataAdapter.fetch(uri));
        SlashProgrammesRdf programmeRdf = getSlashProgrammes(uri);
        if (fetchedContainer instanceof Series) {
            for (SlashProgrammesSeriesContainer series :  programmeRdf.series()) {
                fetchClips(fetchedContainer, series);
            }
            mergeAndWrite(fetchedContainer);
            for (SlashProgrammesSeriesContainer seriesContainer : programmeRdf.series()) {
                fetchSubSeries(seriesContainer, (Series)fetchedContainer);
                fetchEpisodes(seriesContainer.episodeResourceUris(), (Series)fetchedContainer);
            }
        } else if (fetchedContainer instanceof Brand) {
            fetchClips(fetchedContainer, programmeRdf.brand());
            mergeAndWrite(fetchedContainer);
            fetchSeries(programmeRdf.brand().series);
            if (programmeRdf.brand().episodes != null) {
                fetchEpisodes(programmeRdf.brand().episodeResourceUris(), null);
            }
        }
        return fetchedContainer;
    }

    private void mergeAndWrite(Container fetchedContainer) {
        Optional<Content> possibleExistingContent = existingContent(fetchedContainer.getCanonicalUri());
        if (possibleExistingContent.isPresent()) {
            Container existingContainer = (Container)possibleExistingContent.get();
            fetchedContainer = merger.mergeContainers(fetchedContainer, existingContainer);
        }
        store.writeContent(fetchedContainer);
    }

    private void fetchSubSeries(SlashProgrammesSeriesContainer seriesContainer, Series series) {
        for (String subSeriesUri : seriesContainer.seriesResourceUris()) {
            SlashProgrammesRdf programmesRdf = getSlashProgrammes(BbcFeeds.slashProgrammesUriForPid(extractPid(subSeriesUri)));
            for (SlashProgrammesSeriesContainer subSeries : programmesRdf.series()) {
                fetchEpisodes(subSeries.episodeResourceUris(), series);
            }
        }
    }

    protected SlashProgrammesRdf getSlashProgrammes(String uri) {
        try {
            return slashProgrammesClient.get(uri + ".rdf");
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private void fetchEpisodes(List<String> episodes, final Series series) {
        if (episodes != null) {
            for (List<String> episodesPart : Lists.partition(episodes, 10)) {
                ListenableFuture<List<Item>> fetched = Futures.allAsList(Lists.transform(episodesPart, new Function<String, ListenableFuture<Item>>() {
                    @Override
                    public ListenableFuture<Item> apply(@Nullable final String input) {
                        return executor.submit(itemFetchTask(input, series));
                    }
                }));
                try {
                    Futures.get(fetched, 20, TimeUnit.MINUTES, Exception.class);
                } catch (Exception e) {
                    Throwables.propagate(e);
                }
            }
        }
    }

    protected Callable<Item> itemFetchTask(final String episodeUri, final Series series) {
        return new Callable<Item>() {
            @Override
            public Item call() throws Exception {
                String uri = BbcFeeds.slashProgrammesUriForPid(extractPid(episodeUri));
                return fetchItem(uri, series);
            }
        };
    }

    private void fetchSeries(List<SlashProgrammesSeriesRef> series) {
        if (series != null) {
            for (SlashProgrammesSeriesRef seriesRef : series) {
                fetchContainer(BbcFeeds.slashProgrammesUriForPid(extractPid(seriesRef.resourceUri())));
            }
        }
    }

    private void fetchClips(final Container fetchedContainer, SlashProgrammesBase programmeRdf) {
        if (programmeRdf.clips != null) {
            ListenableFuture<List<Clip>> clips = Futures.successfulAsList(Iterables.transform(programmeRdf.clips, new Function<SlashProgrammesClip, ListenableFuture<Clip>>() {
                @Override
                public ListenableFuture<Clip> apply(@Nullable final SlashProgrammesClip input) {
                    return executor.submit(new Callable<Clip>(){
                        @Override
                        public Clip call() throws Exception {
                            return clipAdapter.fetch(BbcFeeds.slashProgrammesUriForPid(extractPid(input.resourceUri())));
                        }
                    });
                }
            }));
            try {
                fetchedContainer.setClips(Iterables.filter(Futures.get(clips, 10, TimeUnit.MINUTES, Exception.class), Predicates.notNull()));
            } catch (Exception e) {
                Throwables.propagate(e);
            }
        }
    }

    private String extractPid(String resourceUri) {
        return resourceUri.substring(resourceUri.lastIndexOf('/')+1, resourceUri.indexOf('#'));
    }

    private Item fetchItem(String uri, Series series) {
        Item fetchedItem = episodeAdapter.fetch(uri);
        if (fetchedItem == null) {
            return null;
        }
        addExtendedData(fetchedItem, extendedDataAdapter.fetch(uri));
        attachSegmentsToVersions(fetchedItem);
        if (fetchedItem instanceof Episode) {
            Episode episode = (Episode)fetchedItem;
            if (series != null) {
                episode.setSeriesNumber(series.getSeriesNumber());
            }
        }
        Optional<Content> possibleExistingContent = existingContent(uri);
        if (possibleExistingContent.isPresent()) {
            fetchedItem = merger.merge(fetchedItem, (Item)possibleExistingContent.get());
        }
        store.writeContent(fetchedItem);
        return fetchedItem;
    }

    private Optional<Content> existingContent(String uri) {
        Alias programmesUrl = new Alias("bbc:programmes:url", uri);
        return store.resolveAliases(ImmutableSet.of(programmesUrl), BBC).get(programmesUrl);
    }

    private void attachSegmentsToVersions(Item fetchedItem) {
        for (Version version : fetchedItem.getVersions()) {
            version.setSegmentEvents(segmentAdapter.fetch(version.getCanonicalUri()));
        }
    }

    private void addExtendedData(Content mainContent, Content extendedData) {
        mainContent.setRelatedLinks(extendedData.getRelatedLinks());
        mainContent.setKeyPhrases(extendedData.getKeyPhrases());
        mainContent.setTopicRefs(extendedData.getTopicRefs());
    }

}
