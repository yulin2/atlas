package org.atlasapi.remotesite.bbc.nitro;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.ScheduleEntry.ItemRefAndBroadcast;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.nitro.extract.NitroBroadcastExtractor;
import org.atlasapi.remotesite.bbc.nitro.extract.NitroUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.metabroadcast.atlas.glycerin.model.PidReference;
import com.metabroadcast.common.time.Clock;

/**
 * {@link NitroBroadcastHandler} which fetches, updates and writes relevant
 * content for the {@link Broadcast}.
 */
public class ContentUpdatingNitroBroadcastHandler implements NitroBroadcastHandler<ImmutableList<Optional<ItemRefAndBroadcast>>> {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final ContentWriter writer;
    private final LocalOrRemoteNitroFetcher localOrRemoteFetcher;
    
    private final NitroBroadcastExtractor broadcastExtractor
        = new NitroBroadcastExtractor();

    public ContentUpdatingNitroBroadcastHandler(ContentResolver resolver, ContentWriter writer, NitroContentAdapter contentAdapter, Clock clock) {
        this.writer = writer;
        this.localOrRemoteFetcher = new LocalOrRemoteNitroFetcher(resolver, contentAdapter, clock);
    }
    
    @Override
    public ImmutableList<Optional<ItemRefAndBroadcast>> handle(Iterable<com.metabroadcast.atlas.glycerin.model.Broadcast> nitroBroadcasts) throws NitroException {
        
        ResolveOrFetchResult<Item> items = localOrRemoteFetcher.resolveOrFetchItem(nitroBroadcasts);
        ImmutableSet<Series> series = localOrRemoteFetcher.resolveOrFetchSeries(items.getFetched().values());
        ImmutableSet<Brand> brands = localOrRemoteFetcher.resolveOrFetchBrand(items.getFetched().values());
        
        ImmutableMap<String, Series> seriesIndex = Maps.uniqueIndex(series, Identified.TO_URI);
        ImmutableMap<String, Brand> brandIndex = Maps.uniqueIndex(brands, Identified.TO_URI);
        
        ImmutableList.Builder<Optional<ItemRefAndBroadcast>> results = ImmutableList.builder();
        
        for (com.metabroadcast.atlas.glycerin.model.Broadcast nitroBroadcast : nitroBroadcasts) {
            try {
                Optional<Broadcast> broadcast = broadcastExtractor.extract(nitroBroadcast);
                checkState(broadcast.isPresent(), "couldn't extract broadcast: %s", nitroBroadcast.getPid());
                
                String itemPid = NitroUtil.programmePid(nitroBroadcast).getPid();
                String itemUri = BbcFeeds.nitroUriForPid(itemPid);
                Item item = items.get(itemUri);
                checkNotNull(item, "No item for broadcast %s: %s", nitroBroadcast.getPid(), itemPid);
                
                addBroadcast(item, versionUri(nitroBroadcast), broadcast.get());
                
                Brand brand = getBrand(item, brandIndex);
                if (brand != null) {
                    writer.createOrUpdate(brand);
                }
                
                Series sery = getSeries(item, seriesIndex);
                if (sery != null) {
                    writer.createOrUpdate(sery);
                }
                writer.createOrUpdate(item);
                
                results.add(Optional.of(new ItemRefAndBroadcast(item, broadcast.get())));
            } catch (Exception e) {
                log.error(nitroBroadcast.getPid(), e);
                results.add(Optional.<ItemRefAndBroadcast>absent());
            }
        }
        return results.build();
    }

    private Series getSeries(Item item, ImmutableMap<String, Series> seriesIndex) {
        if (item instanceof Episode) {
            ParentRef container = ((Episode)item).getSeriesRef();
            if (container != null) {
                return seriesIndex.get(container.getUri());
            }
        }
        return null;
    }

    private Brand getBrand(Item item, ImmutableMap<String, Brand> brandIndex) {
        ParentRef container = item.getContainer();
        if (container != null) {
            return brandIndex.get(container.getUri());
        }
        return null;
    }

    private void addBroadcast(Item item, String versionUri, Broadcast broadcast) {
        Version version = Objects.firstNonNull(getVersion(item, versionUri), newVersion(versionUri));
        version.setBroadcasts(Sets.union(ImmutableSet.of(broadcast), version.getBroadcasts()));
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
    
    private String versionUri(com.metabroadcast.atlas.glycerin.model.Broadcast nitroBroadcast) {
        PidReference pidRef = NitroUtil.versionPid(nitroBroadcast);
        checkArgument(pidRef != null,"Broadcast %s has no version ref", nitroBroadcast.getPid());
        return BbcFeeds.nitroUriForPid(pidRef.getPid());
    }


}
