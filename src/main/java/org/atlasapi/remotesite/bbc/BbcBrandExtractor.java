package org.atlasapi.remotesite.bbc;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesContainerRef;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesEpisodeRef;

public class BbcBrandExtractor implements ContentExtractor<SlashProgrammesContainerRef, Brand> {

	// Some Brands have a lot of episodes, if there are more than this number we
	// only look at the most recent episodes
	private static final int MAX_EPISODES = 200;

	private static final BbcProgrammesGenreMap genreMap = new BbcProgrammesGenreMap();
	private static final Pattern IMAGE_STEM = Pattern.compile("^(.+)_[0-9]+_[0-9]+\\.[a-zA-Z]+$");

	private final BbcProgrammeAdapter itemExtractor;
	private final AdapterLog log;

	public BbcBrandExtractor(BbcProgrammeAdapter itemExtractor, AdapterLog log) {
		this.itemExtractor = itemExtractor;
		this.log = log;
	}

	@Override
	public Brand extract(SlashProgrammesContainerRef brandRef) {
		Brand brand = createBrandFrom(brandRef);
		
		if (brandRef.episodes == null) {
			return brand;
		}

		for (SlashProgrammesEpisodeRef episodeRef : mostRecent(brandRef.episodes)) {
			String pid = pidFrom(episodeRef);
			if (pid == null) {
				log.record(new AdapterLogEntry(Severity.WARN).withUri(episodeRef.resourceUri()).withSource(getClass()).withDescription("Could not extract PID from: " + episodeRef.resourceUri()));
				continue;
			}
			Content found = itemExtractor.fetch("http://www.bbc.co.uk/programmes/" + pid);
			if (!(found instanceof Item)) {
				log.record(new AdapterLogEntry(Severity.WARN).withUri(episodeRef.resourceUri()).withSource(getClass()).withDescription("Expected episode got a brand for PID: " + pid));
				continue;
			} 
			brand.addItem((Item) found); 
		}
		return brand;
	}

	private List<SlashProgrammesEpisodeRef> mostRecent(List<SlashProgrammesEpisodeRef> episodes) {
		if (episodes.size() < MAX_EPISODES) {
			return episodes;
		}
		return episodes.subList(episodes.size() - MAX_EPISODES, episodes.size());
	}

	private Brand createBrandFrom(SlashProgrammesContainerRef brandRef) {
		String brandUri = brandRef.uri();
		Brand brand = new Brand(brandUri, BbcUriCanonicaliser.curieFor(brandUri), Publisher.BBC);
		brand.setTitle(brandRef.title());
		if (brandRef.getDepiction() != null) {
			Matcher matcher = IMAGE_STEM.matcher(brandRef.getDepiction().resourceUri());
			if (matcher.matches()) {
				String base = matcher.group(1);
				brand.setImage(base + BbcProgrammeGraphExtractor.FULL_IMAGE_EXTENSION);
				brand.setThumbnail(base + BbcProgrammeGraphExtractor.THUMBNAIL_EXTENSION);
			}
		}
		brand.setGenres(genreMap.map(brandRef.genreUris()));
		brand.setDescription(brandRef.description());
		return brand;
	}

	private static final Pattern PID_FINDER = Pattern.compile("(b00[a-z0-9]+)");
	
	private String pidFrom(SlashProgrammesEpisodeRef episodeRef) {
		Matcher matcher = PID_FINDER.matcher(episodeRef.resourceUri());
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
	
	

}
