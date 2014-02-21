package org.atlasapi.remotesite.rovi.processing;

import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForSeason;
import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForSeasonHistory;

import java.nio.charset.Charset;

import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.RoviContentWriter;
import org.atlasapi.remotesite.rovi.indexing.IndexAccessException;
import org.atlasapi.remotesite.rovi.model.RoviSeasonHistoryLine;
import org.atlasapi.remotesite.rovi.parsers.RoviSeasonHistoryLineParser;
import org.atlasapi.remotesite.rovi.populators.SeriesPopulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;


public class RoviSeasonLineIngestor extends RoviActionLineIngestor<RoviSeasonHistoryLine, Series> {

    private final static Logger LOG = LoggerFactory.getLogger(RoviSeasonLineIngestor.class); 
    
    private final ContentResolver contentResolver;
    private final LoadingCache<String, Optional<Publisher>> parentPublisherCache;
    
    public RoviSeasonLineIngestor(RoviSeasonHistoryLineParser parser, Charset charset,
            ContentResolver contentResolver, RoviContentWriter contentWriter, LoadingCache<String, Optional<Publisher>> parentPublisherCache) {
        super(parser, charset, contentWriter);
        this.contentResolver = contentResolver;
        this.parentPublisherCache = parentPublisherCache;
    }
    
    @Override
    protected Logger log() {
        return LOG;
    }

    @Override
    protected boolean isToProcess(RoviSeasonHistoryLine parsedLine) {
        return true;
    }

    private SeriesPopulator populator(RoviSeasonHistoryLine parsedLine) {
        return new SeriesPopulator(parsedLine, parentPublisherCache);
    }

    @Override
    protected void populateContent(Series content, RoviSeasonHistoryLine parsedLine)
            throws IndexAccessException {
        populator(parsedLine).populateContent(content);
    }

    @Override
    protected Optional<Series> resolveContent(RoviSeasonHistoryLine parsedLine) {
        // Using alias for resolving here because for deletion records the seasonId is not present
        ImmutableList.Builder<String> uris = ImmutableList.builder();
        uris.add(canonicalUriForSeasonHistory(parsedLine.getSeasonHistoryId()));
        
        if (parsedLine.getSeasonProgramId().isPresent()) {
            uris.add(canonicalUriForSeason(parsedLine.getSeasonProgramId().get()));
        }
        
        Maybe<Identified> maybeResolved = contentResolver.findByUris(uris.build()).getFirstValue();
        
        if (maybeResolved.isNothing()) {
            return Optional.absent();
        }
        
        return Optional.of((Series) maybeResolved.requireValue());
    }

    @Override
    protected Series createContent(RoviSeasonHistoryLine parsedLine) {
        return new Series();
    }

}
