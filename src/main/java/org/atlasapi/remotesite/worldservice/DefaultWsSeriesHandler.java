package org.atlasapi.remotesite.worldservice;

import static org.atlasapi.media.entity.Publisher.WORLD_SERVICE;
import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;
import static org.atlasapi.remotesite.worldservice.WorldServiceIds.curieFor;
import static org.atlasapi.remotesite.worldservice.WorldServiceIds.uriFor;
import static org.atlasapi.remotesite.worldservice.WsGenre.genresForCode;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.worldservice.model.WsSeries;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.base.Maybe;

public class DefaultWsSeriesHandler implements WsSeriesHandler {

    private final ContentResolver resolver;
    private final ContentWriter writer;
    private final AdapterLog log;

    public DefaultWsSeriesHandler(ContentResolver resolver, ContentWriter writer, AdapterLog log) {
        this.resolver = resolver;
        this.writer = writer;
        this.log = log;
    }
    
    @Override
    public void handle(WsSeries series) {
        String seriesUri = null;//uriFor(series);
        
        Maybe<Identified> possibleSeries = resolver.findByCanonicalUris(ImmutableSet.of(seriesUri)).get(seriesUri);
        
        Brand wsBrand = null;
        
        if(possibleSeries.hasValue()) {
            
            Identified resolved = possibleSeries.requireValue();
            
            if(resolved instanceof Brand) {
                wsBrand = (Brand) resolved;
            } else {
                log.record(errorEntry().withDescription("Resolved %s for series %s", resolved.getClass().getSimpleName(), seriesUri));
                return;
            }
            
        } else {
            wsBrand = new Brand(seriesUri, curieFor(series), WORLD_SERVICE);
        }
        
        update(wsBrand, series);
        
        writer.createOrUpdate(wsBrand);
        
    }

    private void update(Brand wsBrand, WsSeries series) {
        
        wsBrand.setMediaType(MediaType.AUDIO);
        wsBrand.setSpecialization(Specialization.RADIO);
        
        wsBrand.setTitle(series.getSeriesTitle());
        wsBrand.setGenres(genresForCode(series.getGenreCode()));
        
    }

}
