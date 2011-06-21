package org.atlasapi.remotesite.bbc;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesBase;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesContainerRef;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesSeriesContainer;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesSeriesRef;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class BbcBrandExtractor  {

	// Some Brands have a lot of episodes, if there are more than this number we
	// only look at the most recent episodes
	private static final int MAX_EPISODES = 5000;

	private static final BbcProgrammesGenreMap genreMap = new BbcProgrammesGenreMap();
	private static final Pattern IMAGE_STEM = Pattern.compile("^(.+)_[0-9]+_[0-9]+\\.[a-zA-Z]+$");

	private final BbcProgrammeAdapter subContentExtractor;
	private final AdapterLog log;

	private final ContentWriter contentWriter;

	public BbcBrandExtractor(BbcProgrammeAdapter subContentExtractor, ContentWriter contentWriter, AdapterLog log) {
		this.subContentExtractor = subContentExtractor;
		this.contentWriter = contentWriter;
		this.log = log;
	}
	
	public Series writeSeries(SlashProgrammesSeriesContainer rdfSeries, Brand brand) {
		Series series = new Series();
		populatePlaylistAttributes(series, rdfSeries);
		List<String> episodeUris = episodesFrom(rdfSeries.episodeResourceUris());
		if (brand != null) {
			series.setParent(brand);
		}
		contentWriter.createOrUpdate(series);
		saveItemsInContainers(episodeUris,  brand == null ? series : brand, series);
    	return series;
	}
	
	public void saveItemsInContainers(List<String> episodeUris, Container container, Series series) {
		for (String episodeUri : mostRecent(episodeUris)) {
			Identified found = subContentExtractor.fetchItem(episodeUri);
			if (!(found instanceof Item)) {
				log.record(new AdapterLogEntry(Severity.WARN).withUri(episodeUri).withSource(getClass()).withDescription("Expected Item for PID: " + episodeUri));
				continue;
			}
			((Item) found).setContainer(container);
			if (series != null) {
				((Episode) found).setSeries(series);
			}
			contentWriter.createOrUpdate((Item) found);
		}
	}

	public Brand writeBrand(SlashProgrammesContainerRef brandRef) {
		Brand brand = new Brand();
		populatePlaylistAttributes(brand, brandRef);
		contentWriter.createOrUpdate(brand);
		writeBrandEpisodes(brandRef, brand);
		return brand;
	}

    private void writeBrandEpisodes(SlashProgrammesContainerRef brandRef, Brand brand) {
        List<String> episodes = brandRef.episodes == null ? ImmutableList.<String>of() : episodesFrom(brandRef.episodeResourceUris());
        
        saveItemsInContainers(episodes, brand, null);

        if (brandRef.series != null) {
        	for (SlashProgrammesSeriesRef seriesRef : brandRef.series) {
        		String seriesPid = BbcFeeds.pidFrom(seriesRef.resourceUri());
        		if (seriesPid == null) {
        			log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withUri(seriesRef.resourceUri()).withDescription("Could not extract PID from series ref " + seriesRef.resourceUri() + " for brand with uri " + brand.getCanonicalUri()));
        			continue;
        		}
        		String uri = BbcFeeds.slashProgrammesUriForPid(seriesPid);
        		Series series = (Series) subContentExtractor.createOrUpdate(uri, brand);
        		if (series == null || series.getMediaType() == null || brand == null || brand.getMediaType() == null) {
        			log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withUri(uri).withDescription("Could not load series with uri " + uri + " for brand with uri " + brand.getCanonicalUri()));
        			continue;
        		}
        		if (brand.getMediaType() != null && ! brand.getMediaType().equals(series.getMediaType())) {
        			series.setMediaType(brand.getMediaType());
        		}
        		if (brand.getSpecialization() != null && ! brand.getSpecialization().equals(series.getSpecialization())) {
                    series.setSpecialization(brand.getSpecialization());
                }
        	}
        }
    }

	private List<String> episodesFrom(List<String> uriFragments) {
		List<String> uris = Lists.newArrayListWithCapacity(uriFragments.size());
		for (String uri : uriFragments) {
			String pid = BbcFeeds.pidFrom(uri);
			if (pid == null) {
				log.record(new AdapterLogEntry(Severity.WARN).withUri(uri).withSource(getClass()).withDescription("Could not extract PID from: " + uri));
				continue;
			}
			uris.add("http://www.bbc.co.uk/programmes/" + pid);
		}
		return uris;
	}

	private <T> List<T> mostRecent(List<T> episodes) {
		if (episodes.size() < MAX_EPISODES) {
			return episodes;
		}
		return episodes.subList(episodes.size() - MAX_EPISODES, episodes.size());
	}

	private void populatePlaylistAttributes(Described container, SlashProgrammesBase containerRefRef) {
		String brandUri = containerRefRef.uri();
		container.setCanonicalUri(brandUri);
		container.setCurie(BbcUriCanonicaliser.curieFor(brandUri));
		container.setPublisher(Publisher.BBC);
		container.setTitle(containerRefRef.title());
		if (containerRefRef.getMasterbrand() != null) {
		    MediaType mediaType = BbcMasterbrandMediaTypeMap.lookup(containerRefRef.getMasterbrand().getResourceUri()).valueOrNull();
		    container.setMediaType(mediaType);
            if (containerRefRef.isFilmFormat()) {
                container.setSpecialization(Specialization.FILM);
            } else if (mediaType != null) {
                container.setSpecialization(MediaType.VIDEO == mediaType ? Specialization.TV : Specialization.RADIO);
            }
		}
		if (containerRefRef.getDepiction() != null) {
			Matcher matcher = IMAGE_STEM.matcher(containerRefRef.getDepiction().resourceUri());
			if (matcher.matches()) {
				String base = matcher.group(1);
				container.setImage(base + BbcProgrammeGraphExtractor.FULL_IMAGE_EXTENSION);
				container.setThumbnail(base + BbcProgrammeGraphExtractor.THUMBNAIL_EXTENSION);
			}
		}
		container.setGenres(genreMap.map(containerRefRef.genreUris()));
		container.setDescription(containerRefRef.description());
	}
}
