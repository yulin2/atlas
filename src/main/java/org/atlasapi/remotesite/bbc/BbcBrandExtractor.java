package org.atlasapi.remotesite.bbc;

import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.content.Container;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesBase;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesClip;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesContainerRef;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesSeriesContainer;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesSeriesRef;
import org.atlasapi.remotesite.bbc.ion.BbcExtendedDataContentAdapter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class BbcBrandExtractor  {

	private static final BbcProgrammesGenreMap genreMap = new BbcProgrammesGenreMap();
	private static final Pattern IMAGE_STEM = Pattern.compile("^(.+)_[0-9]+_[0-9]+\\.[a-zA-Z]+$");
	private static final Pattern SERIES_TITLE = Pattern.compile("Series (\\d+)");

	private final BbcProgrammeGraphExtractor itemExtractor;
	private final BbcProgrammeAdapter subContentExtractor;
	private final AdapterLog log;

	private final ContentWriter contentWriter;
    private final BbcExtendedDataContentAdapter extendedDataAdapter;


	public BbcBrandExtractor(BbcProgrammeAdapter subContentExtractor, ContentWriter contentWriter, BbcProgrammeGraphExtractor itemExtractor, BbcExtendedDataContentAdapter extendedDataAdapter, AdapterLog log) {
		this.subContentExtractor = subContentExtractor;
		this.contentWriter = contentWriter;
        this.itemExtractor = itemExtractor;
        this.extendedDataAdapter = extendedDataAdapter;
		this.log = log;
	}
	
	public Series writeSeries(SlashProgrammesSeriesContainer rdfSeries, Brand brand) {
		Series series = new Series();
		series.withSeriesNumber(extractSeriesNumber(rdfSeries.title()));
		populatePlaylistAttributes(series, rdfSeries);
		List<String> episodeUris = episodesFrom(rdfSeries.episodeResourceUris());
		if (brand != null) {
			series.setParent(brand);
            if (brand.getMediaType() != null && ! brand.getMediaType().equals(series.getMediaType())) {
                series.setMediaType(brand.getMediaType());
            }
            if (brand.getSpecialization() != null && ! brand.getSpecialization().equals(series.getSpecialization())) {
                series.setSpecialization(brand.getSpecialization());
            }
		}
		contentWriter.createOrUpdate(series);
		saveItemsInContainers(episodeUris,  brand == null ? series : brand, series);
    	return series;
	}
	
	private Integer extractSeriesNumber(String title) {
	    Matcher matcher = SERIES_TITLE.matcher(title);
        return matcher.matches() ? Integer.parseInt(matcher.group(1)) : null;
    }

    public void saveItemsInContainers(List<String> episodeUris, Container container, Series series) {
		for (String episodeUri : episodeUris) {
			Identified found = subContentExtractor.fetchItem(episodeUri);
			if (!(found instanceof Item)) {
				log.record(warnEntry().withUri(episodeUri).withSource(getClass()).withDescription("Expected Item for PID: " + episodeUri));
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
        
        if (brandRef.series != null) {
            for (SlashProgrammesSeriesRef seriesRef : brandRef.series) {
                String seriesPid = BbcFeeds.pidFrom(seriesRef.resourceUri());
                if (seriesPid == null) {
                    log.record(warnEntry().withSource(getClass()).withUri(seriesRef.resourceUri()).withDescription("Could not extract PID from series ref " + seriesRef.resourceUri() + " for brand with uri " + brand.getCanonicalUri()));
                    continue;
                }
                String uri = BbcFeeds.slashProgrammesUriForPid(seriesPid);
                subContentExtractor.createOrUpdate(uri, brand);
            }
        }
        
        saveItemsInContainers(episodes, brand, null);
    }

	private List<String> episodesFrom(List<String> uriFragments) {
		List<String> uris = Lists.newArrayListWithCapacity(uriFragments.size());
		for (String uri : uriFragments) {
			String pid = BbcFeeds.pidFrom(uri);
			if (pid == null) {
				log.record(warnEntry().withUri(uri).withSource(getClass()).withDescription("Could not extract PID from: " + uri));
				continue;
			}
			uris.add("http://www.bbc.co.uk/programmes/" + pid);
		}
		return uris;
	}

	private void populatePlaylistAttributes(Container container, SlashProgrammesBase containerRefRef) {
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
		
		Set<SlashProgrammesClip> clipRefs = containerRefRef.clips();
        if (clipRefs != null && !clipRefs.isEmpty()) {
            for (SlashProgrammesClip clipRef: clipRefs) {
                SlashProgrammesRdf clip = subContentExtractor.readSlashProgrammesDataForClip(clipRef);
                
                SlashProgrammesVersionRdf clipVersion = null;
                if (clip.clip().versions() != null && ! clip.clip().versions().isEmpty()) {
                    clipVersion = subContentExtractor.readSlashProgrammesDataForVersion(clip.clip().versions().get(0));
                    itemExtractor.addClipToContent(clip, clipVersion, container);
                }
            }
        }
        
        addExtendedData(container);
	}

    public void addExtendedData(Content content) {
        String brandUri = content.getCanonicalUri();
        try {
            if (extendedDataAdapter.canFetch(brandUri)) {
                Content extendedDataContent = extendedDataAdapter.fetch(brandUri);
                content.setKeyPhrases(extendedDataContent.getKeyPhrases());
                content.setRelatedLinks(extendedDataContent.getRelatedLinks());
                content.setTopicRefs(extendedDataContent.getTopicRefs());
            }
        } catch (Exception e) {
            log.record(warnEntry().withUri(brandUri).withSource(getClass()).withDescription("Could not fetch extended data for %s", brandUri));
        }
    }
}
