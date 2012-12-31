package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import org.atlasapi.media.content.Container;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.BbcProgrammesGenreMap;
import org.atlasapi.remotesite.bbc.ion.model.IonContainer;
import org.atlasapi.remotesite.bbc.ion.model.IonContainerFeed;

import com.google.common.base.Strings;
import com.google.common.primitives.Ints;
import com.metabroadcast.common.base.Maybe;

public class BbcIonContainerAdapter implements BbcContainerAdapter, SiteSpecificAdapter<Container> {

    private static final String CURIE_BASE = "bbc:";
    public static final String CONTAINER_DETAIL_PATTERN = "http://www.bbc.co.uk/iplayer/ion/container/container/%s/category_type/pips/format/json";

    private final BbcIonGenreMap genreMap = new BbcIonGenreMap(new BbcProgrammesGenreMap());
    private final RemoteSiteClient<IonContainerFeed> containerFeedClient;
    private final AdapterLog log;
    
    public BbcIonContainerAdapter(AdapterLog log, RemoteSiteClient<IonContainerFeed> containerFeedClient) {
        this.log = log;
        this.containerFeedClient = containerFeedClient;
    }
    
    private Maybe<IonContainer> getIonContainer(String pid) {
        try {
            return Maybe.firstElementOrNothing(containerFeedClient.get(String.format(CONTAINER_DETAIL_PATTERN, pid)).getBlocklist());
        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Error fetching container info for " + pid));
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
            log.record(errorEntry().withSource(getClass()).withDescription("Expecting brand for %s, got %s ", brandId, ionContainer.getType()));
            return Maybe.nothing();
        }
        
        String pid = ionContainer.getId();
        Brand brand = new Brand(BbcFeeds.slashProgrammesUriForPid(pid), CURIE_BASE+pid, Publisher.BBC);
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
            log.record(errorEntry().withSource(getClass()).withDescription("Expecting series for %s, got %s ", seriesId, ionContainer.getType()));
            return Maybe.nothing();
        }
        
        String pid = ionContainer.getId();
        Series series = new Series(BbcFeeds.slashProgrammesUriForPid(pid), CURIE_BASE+pid, Publisher.BBC);
        
        if (!Strings.isNullOrEmpty(ionContainer.getParentId())) {
            series.setParentRef(new ParentRef(BbcFeeds.slashProgrammesUriForPid(ionContainer.getParentId())));
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
                log.record(warnEntry().withSource(getClass()).withDescription("No mediaType mapping for " + masterbrand));
            }
            
            Maybe<Specialization> maybeSpecialisation = BbcIonMediaTypeMapping.specialisationForService(masterbrand);
            if(maybeSpecialisation.hasValue()) {
                dst.setSpecialization(maybeSpecialisation.requireValue());
            } else {
                log.record(warnEntry().withSource(getClass()).withDescription("No specialisation mapping for " + masterbrand));
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
