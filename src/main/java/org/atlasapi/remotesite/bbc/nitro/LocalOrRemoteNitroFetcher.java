package org.atlasapi.remotesite.bbc.nitro;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.ContentMerger;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.ion.BbcIonMediaTypeMapping;
import org.atlasapi.remotesite.bbc.ion.model.IonBroadcast;
import org.atlasapi.remotesite.bbc.nitro.extract.NitroUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.atlas.glycerin.model.Broadcast;
import com.metabroadcast.atlas.glycerin.model.PidReference;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.DateTimeZones;


public class LocalOrRemoteNitroFetcher {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private final ContentResolver resolver;
    private final NitroContentAdapter contentAdapter;
    private final Clock clock;

    public LocalOrRemoteNitroFetcher(ContentResolver resolver, NitroContentAdapter contentAdapter, Clock clock) {
        this.resolver = resolver;
        this.contentAdapter = contentAdapter;
        this.clock = clock;
    }
    
    public Item resolveOrFetchItem(Broadcast broadcast) throws NitroException {

        final PidReference pidRef = NitroUtil.programmePid(broadcast);
        checkArgument(pidRef != null, "Broadcast %s has no episode ref", broadcast.getPid());
        
        Optional<Item> local = resolveLocally(BbcFeeds.nitroUriForPid(pidRef.getPid()), Item.class);
        
        Item remote = null;
        if (fullFetchPermitted(broadcast) || !local.isPresent()) {
            try {
                remote = contentAdapter.fetchEpisode(pidRef);
            } catch (NitroException e) {
                log.warn("Failed to fetch " + pidRef.getPid(), e);
            }
        }
        
        if (!local.isPresent() && remote == null) {
            throw new NitroException("Failed to resolve or fetch " + pidRef.getPid());
        } 

        if (local.isPresent() && remote != null) {
            return ContentMerger.merge(local.get(), remote);
        } else {
            return Objects.firstNonNull(remote, local.get());
        }
        
    }
    
    protected boolean fullFetchPermitted(Broadcast broadcast) {
        LocalDate today = clock.now().toLocalDate();
        LocalDate broadcastDay = NitroUtil.toDateTime(broadcast.getTxTime().getStart()).toLocalDate();
        
        Maybe<MediaType> mediaType = BbcIonMediaTypeMapping.mediaTypeForService(broadcast.getService().getSid());
        
        // today -4 for radio, today ± 2 for tv
        // radio are more likely to publish clips after a show has been broadcast
        // so with a limited ingest window it is more important to go back as far as possible for radio
        // to ensure that clips are not missed
        if (mediaType.hasValue() && mediaType.requireValue().equals("audio")) {
            return today.plusDays(1).isAfter(broadcastDay) && today.minusDays(5).isBefore(broadcastDay);    
        } else {
            return today.plusDays(3).isAfter(broadcastDay) && today.minusDays(3).isBefore(broadcastDay);
        }
    }
    
    
    public Optional<Series> resolveOrFetchSeries(Item item) {
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
    
    public Optional<Brand> resolveOrFetchBrand(Item item) {
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
    
}
