package org.atlasapi.remotesite.rovi.program;

import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForProgram;
import static org.atlasapi.remotesite.rovi.RoviUtils.canonicalUriForSeason;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.remotesite.rovi.IndexAccessException;
import org.atlasapi.remotesite.rovi.KeyedFileIndex;
import org.atlasapi.remotesite.rovi.series.RoviEpisodeSequenceLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.base.Maybe;

/*
 * Extracts an {@link Episode} from a {@link RoviProgramLine} with {@link RoviShowType} SE (Series Episode)
 */
public class ProgramLineEpisodeExtractor extends ProgramLineBaseItemExtractor<Episode> {

    private final KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex;
    
    private final LoadingCache<String, Optional<Integer>> seasonNumberCache = CacheBuilder.newBuilder()
            .maximumSize(100000)
            .build(new CacheLoader<String, Optional<Integer>>() {

                public Optional<Integer> load(String seasonId) {
                    return getSeasonNumberResolvingSeason(seasonId);
                }
            });
    
    public ProgramLineEpisodeExtractor(
            KeyedFileIndex<String, RoviProgramDescriptionLine> descriptionIndex,
            KeyedFileIndex<String, RoviEpisodeSequenceLine> episodeSequenceIndex,
            ContentResolver contentResolver) {
        super(descriptionIndex, contentResolver);
        this.episodeSequenceIndex = episodeSequenceIndex;
    }

    private final static Logger LOG = LoggerFactory.getLogger(ProgramLineEpisodeExtractor.class);
    
    @Override
    protected Episode createContent() {
        return new Episode();
    }

    @Override
    protected void addItemSpecificData(Episode content, RoviProgramLine programLine) throws IndexAccessException {
        setBrandAndSeriesFromProgramLine(content, programLine);
        setEpisodeNumberIfNumeric(content, programLine);
        setEpisodeTitleIfPresent(content, programLine);
        setDataFromEpisodeSequenceIfPossible(content, programLine);
        setSeasonNumberFromResolvedContentIfNeeded(content, programLine);
    }

    private void setSeasonNumberFromResolvedContentIfNeeded(Episode content,
            RoviProgramLine programLine) throws IndexAccessException {
        if (content.getSeriesNumber() == null && programLine.getSeasonId().isPresent()) {
            Optional<Integer> seasonNumber = seasonNumberCache.getUnchecked(programLine.getSeasonId().get());
            if (seasonNumber.isPresent()) {
                content.setSeriesNumber(seasonNumber.get());
            }
        }
    }
    
    private Optional<Integer> getSeasonNumberResolvingSeason(String seasonId) {
        String seasonCanonicalUri = canonicalUriForSeason(seasonId);
        Maybe<Identified> maybeSeason = contentResolver.findByCanonicalUris(ImmutableList.of(seasonCanonicalUri)).getFirstValue();
        
        if (maybeSeason.hasValue() && maybeSeason.requireValue() instanceof Series) {
            Series season = (Series) maybeSeason.requireValue();
            return Optional.fromNullable(season.getSeriesNumber());
        }
        
        return Optional.absent();
    }

    private void setDataFromEpisodeSequenceIfPossible(Episode content,
            RoviProgramLine programLine) throws IndexAccessException {
        Collection<RoviEpisodeSequenceLine> results = episodeSequenceIndex.getLinesForKey(programLine.getKey());
        RoviEpisodeSequenceLine episodeSequence = Iterables.getFirst(results, null);

        // If found episodeSequence from index then override some values
        if (episodeSequence != null) {
            if (episodeSequence.getEpisodeTitle().isPresent()) {
                content.setTitle(episodeSequence.getEpisodeTitle().get());
            }

            if (episodeSequence.getEpisodeSeasonSequence().isPresent()) {
                content.setEpisodeNumber(episodeSequence.getEpisodeSeasonSequence().get());
            }

            if (episodeSequence.getEpisodeSeasonNumber().isPresent()) {
                content.setSeriesNumber(episodeSequence.getEpisodeSeasonNumber().get());
            }

        }
    }
    
    private void setEpisodeTitleIfPresent(Episode content, RoviProgramLine programLine) {
        if (programLine.getEpisodeTitle().isPresent()) {
            content.setTitle(programLine.getEpisodeTitle().get());
        }
    }

    private void setEpisodeNumberIfNumeric(Episode content, RoviProgramLine programLine) {
        if (programLine.getEpisodeNumber().isPresent() && StringUtils.isNumeric(programLine.getEpisodeNumber().get())) {
            try {
                content.setEpisodeNumber(Integer.valueOf(programLine.getEpisodeNumber().get()));
            } catch (NumberFormatException e) {
                // Ignoring episode number
            }
        }
    }

    private void setBrandAndSeriesFromProgramLine(Episode content, RoviProgramLine programLine) {
        if (programLine.getSeriesId().isPresent()) {
            String seriesCanonicalUri = canonicalUriForProgram(programLine.getSeriesId().get());
            content.setParentRef(new ParentRef(seriesCanonicalUri));
        }
        
        if (programLine.getSeasonId().isPresent()) {
            String seasonCanonicalUri = canonicalUriForSeason(programLine.getSeasonId().get());
            content.setSeriesRef(new ParentRef(seasonCanonicalUri));
        }
    }

    @Override
    protected Logger log() {
        return LOG;
    }

}
