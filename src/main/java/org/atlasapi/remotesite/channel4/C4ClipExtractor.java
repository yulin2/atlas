package org.atlasapi.remotesite.channel4;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.system.RemoteSiteClient;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.metabroadcast.common.http.HttpStatusCodeException;
import com.sun.syndication.feed.atom.Feed;

class C4ClipExtractor {
	
	private final RemoteSiteClient<Feed> client;
	private final C4EpisodesExtractor episodesExtractor;

	public C4ClipExtractor(RemoteSiteClient<Feed> client, C4EpisodesExtractor episodesExtractor) {
		this.client = client;
		this.episodesExtractor = episodesExtractor;
	}
	
	private List<Clip> clipsFrom(Brand brand) {
		try {
			Feed feed = client.get(C4AtomApi.requestForBrand(brand.getCanonicalUri(), "/video.atom"));
			return clipsFrom(feed);
		} catch (HttpStatusCodeException e) {
			if (e.wasResourceNotFound()) {
				return ImmutableList.of();
			}
			throw new RuntimeException(e);
 		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	void fetchAndAddClipsTo(Brand brand, List<Episode> episodes) {
		List<Clip> clips = clipsFrom(brand);
		Map<String, org.atlasapi.media.entity.Item> lookup = toEpisodeLookup(episodes);
		for (Clip clip : clips) {
			Item episode = findEpisode(lookup, clip);
			
			if (episode != null) {
				episode.addClip(clip);
			} else {
				brand.addClip(clip);
			}
		}
	}

	private Item findEpisode(Map<String, org.atlasapi.media.entity.Item> lookup, Clip clip) {
		Matcher matcher = C4AtomApi.SERIES_AND_EPISODE_NUMBER_IN_ANY_URI.matcher(clip.getCanonicalUri());
		if (matcher.find()) {
			Integer series = Integer.valueOf(matcher.group(1));
			Integer episodeNumber = Integer.valueOf(matcher.group(2));	

			org.atlasapi.media.entity.Item item = lookup.get(concatSeriesAndEpNum(series, episodeNumber));
			
			return item;
		}
		return null;
	}
	
    private static Map<String, Item> toEpisodeLookup(Iterable<? extends Item> contents) {
        Map<String, Item> lookup = Maps.newHashMap();
        for (Item item : contents) {
        	if (item instanceof Episode) {
        		Episode episode = (Episode) item;
        		if (episode.getSeriesNumber()  != null && episode.getEpisodeNumber() != null) {
        			lookup.put(concatSeriesAndEpNum(episode), item);
        		}
        	}
        }
        return lookup;
    }
    

	private static String concatSeriesAndEpNum(Episode episode) {
		return concatSeriesAndEpNum(episode.getSeriesNumber() , episode.getEpisodeNumber());
	}
	
	private static String concatSeriesAndEpNum(int seriesNumber, int episodeNumber) {
		return seriesNumber + "-" + episodeNumber;

	}

	private List<Clip> clipsFrom(Feed feed) {
		return episodesExtractor.extractClips(feed);
	}
}
