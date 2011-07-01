package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.BbcIonDeserializer;
import org.atlasapi.remotesite.bbc.ion.model.IonContainer;
import org.atlasapi.remotesite.bbc.ion.model.IonContainerFeed;

import com.google.common.primitives.Ints;
import com.google.gson.reflect.TypeToken;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpClientBuilder;

public class BbcIonContainerFetcherClient implements BbcContainerFetcherClient {

    private static final String CURIE_BASE = "bbc:";
    private static final String EPISODE_DETAIL_PATTERN = "http://www.bbc.co.uk/iplayer/ion/container/container/%s/format/json";

    private final BbcIonDeserializer<IonContainerFeed> ionDeserialiser = BbcIonDeserializers.deserializerForType(new TypeToken<IonContainerFeed>(){});
    
    private final SimpleHttpClient httpClient = new SimpleHttpClientBuilder()
        .withUserAgent(HttpClients.ATLAS_USER_AGENT)
        .withSocketTimeout(30, TimeUnit.SECONDS)
        .withTransformer(new HttpResponseTransformer<IonContainerFeed>() {

                @Override
                public IonContainerFeed transform(HttpResponse response) throws HttpException, IOException {
                    return ionDeserialiser.deserialise(new InputStreamReader(response.getEntity().getContent()));
                }
            }).build();
    
    private final AdapterLog log;
    
    public BbcIonContainerFetcherClient(AdapterLog log) {
        this.log = log;
    }
    
    private Maybe<IonContainer> getIonContainer(String pid) {
        try {
            return Maybe.firstElementOrNothing(((IonContainerFeed)httpClient.get(String.format(EPISODE_DETAIL_PATTERN, pid)).transform()).getBlocklist());
        } catch (HttpException e) {
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
        
        IonContainer ionContainer = maybeIonContainer.requireValue();
        
        if(!ionContainer.getType().equals("brand")) {
            log.record(errorEntry().withSource(getClass()).withDescription("Expecting brand for %s, got %s ", brandId, ionContainer.getType()));
            return Maybe.nothing();
        }
        
        String pid = ionContainer.getId();
        Brand brand = new Brand(BbcFeeds.slashProgrammesUriForPid(pid), CURIE_BASE+pid, Publisher.BBC);
        
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
        
        IonContainer ionContainer = maybeIonContainer.requireValue();
        
        if(!ionContainer.getType().equals("brand")) {
            log.record(errorEntry().withSource(getClass()).withDescription("Expecting brand for %s, got %s ", seriesId, ionContainer.getType()));
            return Maybe.nothing();
        }
        
        String pid = ionContainer.getId();
        Series series = new Series(BbcFeeds.slashProgrammesUriForPid(pid), CURIE_BASE+pid, Publisher.BBC);
        
        setCommonFields(ionContainer, series);
        series.withSeriesNumber(Ints.saturatedCast(ionContainer.getPosition()));
        BbcImageUrlCreator.addIplayerImagesTo(ionContainer.getId(), series);

        return Maybe.just(series);
        
    }
    
    private void setCommonFields(IonContainer src, Container<Episode> dst) {
        dst.setTitle(src.getTitle());
        dst.setDescription(src.getMediumSynopsis() == null ? src.getShortSynopsis() : src.getMediumSynopsis());
        dst.setLastUpdated(src.getUpdated());
        
        Maybe<MediaType> maybeMediaType = BbcIonMediaTypeMapping.mediaTypeForService(src.getMasterbrand());
        if(maybeMediaType.hasValue()) {
            dst.setMediaType(maybeMediaType.requireValue());
        } else {
            log.record(warnEntry().withSource(getClass()).withDescription("No mediaType mapping for " + src.getMasterbrand()));
        }
        
        Maybe<Specialization> maybeSpecialisation = BbcIonMediaTypeMapping.specialisationForService(src.getMasterbrand());
        if(maybeSpecialisation.hasValue()) {
            dst.setSpecialization(maybeSpecialisation.requireValue());
        } else {
            log.record(warnEntry().withSource(getClass()).withDescription("No specialisation mapping for " + src.getMasterbrand()));
        }
    }
}
