package org.atlasapi.remotesite.bbc.ion;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.atlasapi.media.entity.Publisher.BBC;
import static org.atlasapi.remotesite.bbc.ion.BbcIonContainerAdapter.CONTAINER_DETAIL_PATTERN;

import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.content.ContentResolver;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.bbc.BbcAliasCompiler;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.BbcProgrammesGenreMap;
import org.atlasapi.remotesite.bbc.ion.model.IonContainer;
import org.atlasapi.remotesite.bbc.ion.model.IonContainerFeed;
import org.atlasapi.remotesite.bbc.ion.model.IonContributor;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisode;
import org.atlasapi.remotesite.bbc.ion.model.IonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import com.metabroadcast.common.base.Maybe;

public abstract class BaseBbcIonEpisodeItemExtractor {
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String FILM_FORMAT_ID = "PT007";
    protected static final String CURIE_BASE = "bbc:";

    private final BbcIonContributorPersonExtractor personExtractor = new BbcIonContributorPersonExtractor();
    private final BbcIonGenreMap genreMap = new BbcIonGenreMap(new BbcProgrammesGenreMap());
    
    private final RemoteSiteClient<IonContainerFeed> containerClient;
    private final ContentResolver contentResolver;
    
    public BaseBbcIonEpisodeItemExtractor(RemoteSiteClient<IonContainerFeed> containerClient, ContentResolver contentResolver) {
        this.containerClient = containerClient;
        this.contentResolver = checkNotNull(contentResolver);
    }

    protected Item extract(IonEpisode source) {
        Item item = null;
        if (source.getIsFilm() || isFilmFormat(source)) {
            item = new Film(BbcFeeds.slashProgrammesUriForPid(source.getId()), CURIE_BASE+source.getId(), BBC);
            item.setMediaType(MediaType.VIDEO);
            item.setSpecialization(Specialization.FILM);
            
        } else if (!Strings.isNullOrEmpty(source.getBrandId()) || !Strings.isNullOrEmpty(source.getSeriesId())) {
            item = new Episode(BbcFeeds.slashProgrammesUriForPid(source.getId()), CURIE_BASE+source.getId(), BBC);
            setEpisodeDetails((Episode)item, source);
            setMediaTypeAndSpecialisation(item, source);
            
        } else {
            item = new Item(BbcFeeds.slashProgrammesUriForPid(source.getId()), CURIE_BASE+source.getId(), BBC);
            setMediaTypeAndSpecialisation(item, source);
        }
        return setItemDetails(item, source);
    }

    private boolean isFilmFormat(IonEpisode source) {
        if (source.getFormats() != null) {
            for (IonFormat format : source.getFormats()) {
                if (FILM_FORMAT_ID.equals(format.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void setEpisodeDetails(Episode item, IonEpisode episode) {
        String seriesPid = episode.getSeriesId();
        if(!Strings.isNullOrEmpty(seriesPid)) {
            Optional<Content> possibleContainer = resolvePid(seriesPid);
            checkState(possibleContainer.isPresent(), "No container %s for %s", seriesPid, episode.getId());
            item.setSeries((Series)possibleContainer.get());
        }
        
        if(episode.getPosition() == null) {
            return;
        }
        
        String subseriesId = episode.getSubseriesId();

        if (Strings.isNullOrEmpty(subseriesId)) {
            item.setEpisodeNumber(Ints.saturatedCast(episode.getPosition()));
            return;
        }

        if (item.getPartNumber() == null && containerClient != null) {
            try {
                IonContainer subseries = Iterables.getOnlyElement(containerClient.get(String.format(CONTAINER_DETAIL_PATTERN, subseriesId)).getBlocklist());
                item.setEpisodeNumber(Ints.saturatedCast(subseries.getPosition()));
                item.setPartNumber(Ints.saturatedCast(episode.getPosition()));
            } catch (Exception e) {
                log.warn("Updating item {}, couldn't fetch subseries {}", item, subseriesId);
            }
        }
    }

    protected Item setItemDetails(Item item, IonEpisode episode) {
        item.addAlias(item.getCanonicalUri());
        item.setTitle(getTitle(episode));
        item.setDescription(episode.getSynopsis());
        // TODO new alias
        item.setAliasUrls(BbcAliasCompiler.bbcAliasUrisFor(item.getCanonicalUri()));
        item.setIsLongForm(true);
        item.setLastUpdated(episode.getUpdated());
        item.setGenres(genreMap.fromIon(episode.getGenres()));
        if (!Strings.isNullOrEmpty(episode.getId())) {
            BbcImageUrlCreator.addImagesTo(episode.getMyImageBaseUrl().toString(), episode.getId(), item);
        }
        
        if (episode.getContributors() != null) {
            for (IonContributor contributor: episode.getContributors()) {
                Maybe<CrewMember> possiblePerson = personExtractor.extract(contributor);
                if (possiblePerson.hasValue()) {
                    item.addPerson(possiblePerson.requireValue());
                } else {
                    log.warn("Unknown person: {}", contributor.getRoleName());
                }
            }
        }
        
        String containerPid = episode.getToplevelContainerId();
        if (!Strings.isNullOrEmpty(containerPid)) {
            Optional<Content> possibleContainer = resolvePid(containerPid);
            checkState(possibleContainer.isPresent(), "No container %s for %s", containerPid, episode.getId());
            item.setContainer((Container)possibleContainer.get());
        }

        return item;
    }

    private Optional<Content> resolvePid(String pid) {
        String containerUri = BbcFeeds.slashProgrammesUriForPid(pid);
        Optional<Content> possibleContainer = contentResolver.resolveAliases(ImmutableList.of(containerUri), Publisher.BBC).get(containerUri);
        return possibleContainer;
    }

    protected void setMediaTypeAndSpecialisation(Item item, IonEpisode episode) {
        if(!Strings.isNullOrEmpty(episode.getMediaType())) {
            String mediaType = episode.getMediaType();
            if (mediaType.equals("audio")) {
                item.setMediaType(MediaType.AUDIO);
                item.setSpecialization(Specialization.RADIO);
            } else if (mediaType.equals("video")) {
                item.setMediaType(MediaType.VIDEO);
                item.setSpecialization(Specialization.TV);
            }
        }
    }
    
    protected String getTitle(IonEpisode episode) {
        String title = !Strings.isNullOrEmpty(episode.getOriginalTitle()) ? episode.getOriginalTitle() : episode.getTitle();
        if(!Strings.isNullOrEmpty(episode.getSubseriesTitle())) {
            title = String.format("%s %s", episode.getSubseriesTitle(), title);
        }
        return title;
    }
}
