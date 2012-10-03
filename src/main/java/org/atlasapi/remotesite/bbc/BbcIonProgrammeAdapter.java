package org.atlasapi.remotesite.bbc;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Version;
import org.atlasapi.media.segment.SegmentEvent;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesBase;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesClip;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesSeriesContainer;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesSeriesRef;
import org.atlasapi.remotesite.bbc.ion.BbcExtendedDataContentAdapter;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class BbcIonProgrammeAdapter implements SiteSpecificAdapter<Content> {

    private ContentWriter writer;
    private SiteSpecificAdapter<Item> episodeAdapter;
    private BbcExtendedDataContentAdapter extendedDataAdapter;
    private SiteSpecificAdapter<List<SegmentEvent>> segmentAdapter;
    private RemoteSiteClient<SlashProgrammesRdf> slashProgrammesClient;
    private SiteSpecificAdapter<Container> containerAdapter;
    private SiteSpecificAdapter<Clip> clipAdapter;
    private ListeningExecutorService executor;

    public BbcIonProgrammeAdapter(ContentWriter writer, SiteSpecificAdapter<Item> episodeAdapter, BbcExtendedDataContentAdapter extendedDataAdapter, SiteSpecificAdapter<List<SegmentEvent>> segmentAdapter, RemoteSiteClient<SlashProgrammesRdf> slashProgrammesClient, SiteSpecificAdapter<Container> containerAdapter, SiteSpecificAdapter<Clip> clipAdapter, ExecutorService executor) {
        this.writer = writer;
        this.episodeAdapter = episodeAdapter;
        this.extendedDataAdapter = extendedDataAdapter;
        this.segmentAdapter = segmentAdapter;
        this.slashProgrammesClient = slashProgrammesClient;
        this.containerAdapter = containerAdapter;
        this.clipAdapter = clipAdapter;
        this.executor = MoreExecutors.listeningDecorator(executor);
    }
    
    @Override
    public boolean canFetch(String uri) {
        return BbcFeeds.isACanonicalSlashProgrammesUri(uri);
    }

    @Override
    public Content fetch(String uri) {
        Item fetchedItem = fetchItem(uri);
        if (fetchedItem != null) {
            writer.createOrUpdate(fetchedItem);
            return fetchedItem;
        }
        return fetchContainer(uri);
    }

    protected Container fetchContainer(String uri) {
        Container fetchedContainer = containerAdapter.fetch(uri);
        if (fetchedContainer == null) {
            return null;
        }
        mergeExtendedData(fetchedContainer, extendedDataAdapter.fetch(uri));
        SlashProgrammesRdf programmeRdf = getSlashProgrammes(uri);
        if (fetchedContainer instanceof Series) {
            for (SlashProgrammesSeriesContainer series :  programmeRdf.series()) {
                fetchClips(fetchedContainer, series);
            }
            writer.createOrUpdate(fetchedContainer);
            for (SlashProgrammesSeriesContainer seriesContainer : programmeRdf.series()) {
                fetchSubSeries(seriesContainer, (Series)fetchedContainer);
                fetchEpisodes(seriesContainer.episodeResourceUris(), (Series)fetchedContainer);
            }
        } else if (fetchedContainer instanceof Brand) {
            fetchClips(fetchedContainer, programmeRdf.brand());
            writer.createOrUpdate(fetchedContainer);
            fetchSeries(programmeRdf.brand().series);
            if (programmeRdf.brand().episodes != null) {
                fetchEpisodes(programmeRdf.brand().episodeResourceUris(), null);
            }
        }
        return fetchedContainer;
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

    private void fetchEpisodes(List<String> episodes, Series series) {
        if (episodes != null) {
            for (List<String> episodesPart : Lists.partition(episodes, 10)) {
                ListenableFuture<List<Item>> fetched = Futures.allAsList(Lists.transform(episodesPart, new Function<String, ListenableFuture<Item>>() {
                    @Override
                    public ListenableFuture<Item> apply(@Nullable final String input) {
                        return executor.submit(itemFetchTask(input));
                    }
                }));
                try {
                    for (Item item : Iterables.filter(Futures.get(fetched, 20, TimeUnit.MINUTES, Exception.class), Predicates.notNull())) {
                        if (series != null && item instanceof Episode) {
                            ((Episode)item).setSeriesNumber(series.getSeriesNumber());
                        }
                        writer.createOrUpdate(item);
                    }
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            }
        }
    }

    protected Callable<Item> itemFetchTask(final String episodeUri) {
        return new Callable<Item>() {
            @Override
            public Item call() throws Exception {
                return fetchItem(BbcFeeds.slashProgrammesUriForPid(extractPid(episodeUri)));
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

    protected Item fetchItem(String uri) {
        Item fetchedItem = episodeAdapter.fetch(uri);
        if (fetchedItem == null) {
            return null;
        }
        mergeExtendedData(fetchedItem, extendedDataAdapter.fetch(uri));
        attachSegmentsToVersions(fetchedItem);
        return fetchedItem;
    }

    private void attachSegmentsToVersions(Item fetchedItem) {
        for (Version version : fetchedItem.getVersions()) {
            version.setSegmentEvents(segmentAdapter.fetch(version.getCanonicalUri()));
        }
    }

    private void mergeExtendedData(Content mainContent, Content extendedData) {
        mainContent.setRelatedLinks(extendedData.getRelatedLinks());
        mainContent.setKeyPhrases(extendedData.getKeyPhrases());
        mainContent.setTopicRefs(extendedData.getTopicRefs());
    }

}
