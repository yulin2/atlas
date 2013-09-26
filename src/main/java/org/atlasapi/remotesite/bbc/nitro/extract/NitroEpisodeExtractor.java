package org.atlasapi.remotesite.bbc.nitro.extract;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.nitro.v1.NitroFormat;
import org.atlasapi.remotesite.bbc.nitro.v1.NitroGenreGroup;
import org.joda.time.DateTime;

import com.metabroadcast.atlas.glycerin.model.AncestorsTitles;
import com.metabroadcast.atlas.glycerin.model.Episode;
import com.metabroadcast.atlas.glycerin.model.Image;
import com.metabroadcast.atlas.glycerin.model.Synopses;
import com.metabroadcast.common.time.Clock;

/**
 * <p>
 * A {@link BaseNitroItemExtractor} for extracting {@link Item}s from
 * {@link Episode} sources.
 * </p>
 * 
 * <p>
 * Creates and {@link Item} or {@link org.atlasapi.media.entity.Episode Atlas
 * Episode} and sets the parent and episode number fields as necessary.
 * </p>
 * 
 * @see BaseNitroItemExtractor
 * @see NitroContentExtractor
 */
public final class NitroEpisodeExtractor extends BaseNitroItemExtractor<Episode, Item> {

    private static final String FILM_FORMAT_ID = "PT007";
    private final ContentExtractor<List<NitroGenreGroup>, Set<String>> genresExtractor
        = new NitroGenresExtractor();

    public NitroEpisodeExtractor(Clock clock) {
        super(clock);
    }

    @Override
    protected Item createContent(NitroItemSource<Episode> source) {
        if (isFilmFormat(source)) {
            return new Film();
        }
        if (source.getProgramme().getEpisodeOf() == null) {
            return new Item();
        }
        return new org.atlasapi.media.entity.Episode();
    }

    private boolean isFilmFormat(NitroItemSource<Episode> source) {
        for (NitroFormat format : source.getFormats()) {
            if (FILM_FORMAT_ID.equals(format.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected String extractPid(NitroItemSource<Episode> source) {
        return source.getProgramme().getPid();
    }

    @Override
    protected String extractTitle(NitroItemSource<Episode> source) {
        return source.getProgramme().getTitle();
    }

    @Override
    protected Synopses extractSynopses(NitroItemSource<Episode> source) {
        return source.getProgramme().getSynopses();
    }

    @Override
    protected Image extractImage(NitroItemSource<Episode> source) {
        return source.getProgramme().getImage();
    }

    @Override
    protected void extractAdditionalItemFields(NitroItemSource<Episode> source, Item item, DateTime now) {
        Episode episode = source.getProgramme();
        if (item.getTitle() == null) {
            item.setTitle(episode.getPresentationTitle());
        }
        if (episode.getEpisodeOf() != null) {
            BigInteger position = episode.getEpisodeOf().getPosition();
            org.atlasapi.media.entity.Episode episodeContent = (org.atlasapi.media.entity.Episode) item;
            if (position != null) {
                episodeContent.setEpisodeNumber(position.intValue());
            }
            if ("series".equals(episode.getEpisodeOf().getResultType())) {
                ParentRef parent = new ParentRef(
                    BbcFeeds.nitroUriForPid(episode.getEpisodeOf().getPid())
                );
                episodeContent.setSeriesRef(parent);
                episodeContent.setParentRef(parent);
            }
        }
        if (episode.getAncestorsTitles() != null) {
            AncestorsTitles ancestors = episode.getAncestorsTitles();
            if (ancestors.getBrand() != null) {
                String brandUri = BbcFeeds.nitroUriForPid(ancestors.getBrand().getPid());
                item.setParentRef(new ParentRef(brandUri));
            }
        }
        String mediaType = source.getProgramme().getMediaType();
        if (mediaType != null) {
            item.setMediaType(MediaType.fromKey(mediaType.toLowerCase()).orNull());
        }
        if (MediaType.VIDEO.equals(item.getMediaType())) {
            item.setSpecialization(Specialization.TV);
        } else if (MediaType.AUDIO.equals(item.getMediaType())) {
            item.setSpecialization(Specialization.RADIO);
        }
        item.setGenres(genresExtractor.extract(source.getGenres()));
    }
}
