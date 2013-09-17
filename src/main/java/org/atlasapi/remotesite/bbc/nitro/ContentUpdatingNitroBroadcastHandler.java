package org.atlasapi.remotesite.bbc.nitro;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.ScheduleEntry.ItemRefAndBroadcast;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.nitro.extract.NitroBroadcastExtractor;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.atlas.glycerin.model.PidReference;
import com.metabroadcast.common.base.Maybe;

/**
 * {@link NitroBroadcastHandler} which fetches, updates and writes relevant
 * content for the {@link Broadcast}.
 */
public class ContentUpdatingNitroBroadcastHandler implements NitroBroadcastHandler<ItemRefAndBroadcast> {

    private final ContentResolver resolver;
    private final ContentWriter writer;
    private final NitroContentAdapter contentAdapter;
    
    private final NitroBroadcastExtractor broadcastExtractor
        = new NitroBroadcastExtractor();

    public ContentUpdatingNitroBroadcastHandler(ContentResolver resolver, ContentWriter writer, NitroContentAdapter contentAdapter) {
        this.resolver = resolver;
        this.writer = writer;
        this.contentAdapter = contentAdapter;
    }
    
    @Override
    public ItemRefAndBroadcast handle(com.metabroadcast.atlas.glycerin.model.Broadcast nitroBroadcast) throws NitroException {
        
        Item item = resolveOrFetchItem(nitroBroadcast);
        Optional<Series> series = resolveOrFetchSeries(item);
        Optional<Brand> brand = resolveOrFetchBrand(item);
        
        Broadcast broadcast = broadcastExtractor.extract(nitroBroadcast);
        addBroadcast(item, versionUri(nitroBroadcast), broadcast);
        if (brand.isPresent()) {
            writer.createOrUpdate(brand.get());
        } 
        if (series.isPresent()) {
            writer.createOrUpdate(series.get());
        }
        writer.createOrUpdate(item);
        
        return new ItemRefAndBroadcast(item, broadcast);
    }

    private void addBroadcast(Item item, String versionUri, Broadcast broadcast) {
        Version version = Objects.firstNonNull(getVersion(item, versionUri), newVersion(versionUri));
        version.addBroadcast(broadcast);
        item.addVersion(version);
    }

    private Version getVersion(Item item, String versionUri) {
        for (Version version : item.getVersions()) {
            if (versionUri.equals(version.getCanonicalUri())) {
                return version;
            }
        }
        return null;
    }

    private Version newVersion(String versionUri) {
        Version version = new Version();
        version.setCanonicalUri(versionUri);
        return version;
    }

    private Item resolveOrFetchItem(com.metabroadcast.atlas.glycerin.model.Broadcast nitroBroadcast) throws NitroException {
        final PidReference pidRef = pidRefForType(nitroBroadcast, "episode");
        checkArgument(pidRef != null,"Broadcast %s has no episode ref", nitroBroadcast.getPid());
        
        Optional<Item> local = resolveLocally(BbcFeeds.nitroUriForPid(pidRef.getPid()), Item.class);
        try {
            return local.or(new Supplier<Item>() {
                @Override
                public Item get() {
                    try {
                        return contentAdapter.fetchEpisode(pidRef);
                    } catch (NitroException e) {
                        throw new FetchException(pidRef.getPid(), e);
                    }
                }
            });
        } catch (FetchException e) {
            throw (NitroException) e.getCause();
        }
    }

    private Optional<Series> resolveOrFetchSeries(Item item) {
        if (item instanceof Episode) {
            ParentRef seriesRef = ((Episode)item).getSeriesRef();
            if (seriesRef != null) {
                final String seriesUri = seriesRef.getUri();
                return Optional.of(resolveLocally(seriesUri, Series.class).or(new Supplier<Series>() {
                    @Override
                    public Series get() {
                        PidReference pidRef = new PidReference();
                        pidRef.setPid(BbcFeeds.pidFrom(seriesUri));
                        pidRef.setResultType("series");
                        try {
                            return contentAdapter.fetchSeries(pidRef);
                        } catch (NitroException e) {
                            throw new FetchException(pidRef.getPid(), e);
                        }
                    }
                }));
            }
        }
        return Optional.absent();
    }

    private Optional<Brand> resolveOrFetchBrand(Item item) {
        ParentRef containerRef = item.getContainer();
        if (containerRef != null && !inTopLevelSeries(item)) {
            final String containerUri = containerRef.getUri();
            return Optional.of(resolveLocally(containerUri, Brand.class).or(new Supplier<Brand>() {
                @Override
                public Brand get() {
                    PidReference pidRef = new PidReference();
                    pidRef.setPid(BbcFeeds.pidFrom(containerUri));
                    pidRef.setResultType("brand");
                    try {
                        return contentAdapter.fetchBrand(pidRef);
                    } catch (NitroException e) {
                        throw new FetchException(pidRef.getPid(), e);
                    }
                }
            }));
        }
        return Optional.absent();
    }


    private boolean inTopLevelSeries(Item item) {
        if (item instanceof Episode) {
            Episode ep = (Episode)item;
            return ep.getSeriesRef() != null 
                && ep.getContainer().equals(ep.getSeriesRef());
        }
        return false;
    }

    private <T extends Content> Optional<T> resolveLocally(String uri, Class<T> expectedType) {
        Maybe<Identified> possible = resolver.findByCanonicalUris(ImmutableList.of(uri)).get(uri);
        if (possible.hasValue()) {
            Identified ided = possible.requireValue();
            checkState(expectedType.isAssignableFrom(ided.getClass()),  "%s %s not %s", 
                    ided.getClass().getSimpleName(), uri, expectedType.getSimpleName());
            return Optional.of(expectedType.cast(ided));
        }
        return Optional.absent();
    }
    
    private String versionUri(com.metabroadcast.atlas.glycerin.model.Broadcast nitroBroadcast) {
        PidReference pidRef = pidRefForType(nitroBroadcast, "version");
        checkArgument(pidRef != null,"Broadcast %s has no version ref", nitroBroadcast.getPid());
        return BbcFeeds.nitroUriForPid(pidRef.getPid());
    }

    private PidReference pidRefForType(com.metabroadcast.atlas.glycerin.model.Broadcast nitroBroadcast,
            String type) {
        for (PidReference pidRef : nitroBroadcast.getBroadcastOf()) {
            if (type.equals(pidRef.getResultType())) {
                return pidRef;
            }
        }
        return null;
    }
}
