package org.atlasapi.remotesite.bbc.ion;

import static com.google.common.base.Preconditions.checkState;

import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.ContentResolver;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.BbcProgrammesGenreMap;
import org.atlasapi.remotesite.bbc.ion.model.IonContainer;
import org.atlasapi.remotesite.bbc.ion.model.IonContainerFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import com.metabroadcast.common.base.Maybe;

public class BbcIonContainerAdapter implements BbcContainerAdapter, SiteSpecificAdapter<Container> {
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String CURIE_BASE = "bbc:";
    public static final String CONTAINER_DETAIL_PATTERN = "http://www.bbc.co.uk/iplayer/ion/container/container/%s/category_type/pips/format/json";

    private final BbcIonGenreMap genreMap = new BbcIonGenreMap(new BbcProgrammesGenreMap());
    
    private final RemoteSiteClient<IonContainerFeed> containerFeedClient;
    private final ContentResolver contentResolver;
    
    public BbcIonContainerAdapter(RemoteSiteClient<IonContainerFeed> containerFeedClient, ContentResolver contentResolver) {
        this.containerFeedClient = containerFeedClient;
        this.contentResolver = contentResolver;
    }
    
    private Maybe<IonContainer> getIonContainer(String pid) {
        try {
            return Maybe.firstElementOrNothing(containerFeedClient.get(String.format(CONTAINER_DETAIL_PATTERN, pid)).getBlocklist());
        } catch (Exception e) {
            log.error("Error fetching container info for {}", pid);
            return Maybe.nothing();
        }
    }
    
    @Override
    public Maybe<Brand> createBrand(String brandId) {
        
        Maybe<IonContainer> maybeIonContainer = getIonContainer(brandId);
        if(maybeIonContainer.isNothing()) {
            return Maybe.nothing();
        }
        
        return extractBrand(brandId, maybeIonContainer.requireValue());
    }

    protected Maybe<Brand> extractBrand(String brandId, IonContainer ionContainer) {
        if(!ionContainer.getType().equals("brand")) {
            log.error("Expecting brand for {}, got {}", brandId, ionContainer.getType());
            return Maybe.nothing();
        }
        
        String pid = ionContainer.getId();
        String programmesUri = BbcFeeds.slashProgrammesUriForPid(pid);
        Brand brand = new Brand(programmesUri, CURIE_BASE+pid, Publisher.BBC);
        brand.addAlias(programmesUri);
        brand.setGenres(genreMap.fromIon(ionContainer.getGenres()));
        setCommonFields(ionContainer, brand);
        BbcImageUrlCreator.addIplayerImagesTo(ionContainer.getId(), brand);
        
        return Maybe.just(brand);
    }

    @Override
    public Maybe<Series> createSeries(String seriesId) {
        
        Maybe<IonContainer> maybeIonContainer = getIonContainer(seriesId);
        if(maybeIonContainer.isNothing()) {
            return Maybe.nothing();
        }
        
        return extractSeries(seriesId, maybeIonContainer.requireValue());
        
    }

    protected Maybe<Series> extractSeries(String seriesId, IonContainer ionContainer) {
        if(!ionContainer.getType().equals("series")) {
            log.error("Expecting series for {}, got {}", seriesId, ionContainer.getType());
            return Maybe.nothing();
        }
        
        String pid = ionContainer.getId();
        Series series = new Series(BbcFeeds.slashProgrammesUriForPid(pid), CURIE_BASE+pid, Publisher.BBC);
        series.addAlias(series.getCanonicalUri());
        
        if (!Strings.isNullOrEmpty(ionContainer.getParentId())) {
            String containerUri = BbcFeeds.slashProgrammesUriForPid(ionContainer.getParentId());
            Optional<Content> possibleContainer = contentResolver.resolveAliases(ImmutableList.of(containerUri), Publisher.BBC).get(containerUri);
            checkState(possibleContainer.isPresent(), "No container %s for %s", ionContainer.getParentId(), pid);
            series.setParent((Brand)possibleContainer.get());
        }
        
        setCommonFields(ionContainer, series);
        series.withSeriesNumber(getSeriesNumber(ionContainer));
        BbcImageUrlCreator.addIplayerImagesTo(ionContainer.getId(), series);

        return Maybe.just(series);
    }

    // abuse the ION position to get a series number. 
    // top-level series have a position of 0 in ION but it should be null in Atlas.
    private Integer getSeriesNumber(IonContainer ionContainer) {
        int seriesNumber = Ints.saturatedCast(ionContainer.getPosition());
        return seriesNumber > 0 ? seriesNumber : null;
    }
    
    private void setCommonFields(IonContainer src, Container dst) {
        dst.setTitle(src.getTitle());
        dst.setDescription(src.getMediumSynopsis() == null ? src.getShortSynopsis() : src.getMediumSynopsis());
        dst.setLastUpdated(src.getUpdated());
        
        String masterbrand = src.getMasterbrand();
        if(!Strings.isNullOrEmpty(masterbrand)) {
            Maybe<MediaType> maybeMediaType = BbcIonMediaTypeMapping.mediaTypeForService(masterbrand);
            if(maybeMediaType.hasValue()) {
                dst.setMediaType(maybeMediaType.requireValue());
            } else {
                log.warn("No mediaType mapping for {}", masterbrand);
            }
            
            Maybe<Specialization> maybeSpecialisation = BbcIonMediaTypeMapping.specialisationForService(masterbrand);
            if(maybeSpecialisation.hasValue()) {
                dst.setSpecialization(maybeSpecialisation.requireValue());
            } else {
                log.warn("No specialisation mapping for {}" ,masterbrand);
            }
        }
    }

    @Override
    public Maybe<IonContainer> getSubseries(String subseriesId) {
        return getIonContainer(subseriesId);
    }

    @Override
    public Container fetch(String uri) {
        String pid = BbcFeeds.pidFrom(uri);
        Maybe<IonContainer> ionContainer = getIonContainer(pid);
        if (ionContainer.isNothing()) {
            return null;
        }
        IonContainer container = ionContainer.requireValue();
        if("brand".equals(container.getType())) {
            return extractBrand(pid, container).valueOrNull();
        }
        if("series".equals(container.getType())) {
            return extractSeries(pid, container).valueOrNull();
        }
        return null;
    }

    @Override
    public boolean canFetch(String uri) {
        return BbcFeeds.isACanonicalSlashProgrammesUri(uri);
    }
    
}
