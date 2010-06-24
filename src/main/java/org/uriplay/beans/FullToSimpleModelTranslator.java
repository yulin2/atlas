package org.uriplay.beans;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;

import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Broadcast;
import org.uriplay.media.entity.Countries;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Episode;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Playlist;
import org.uriplay.media.entity.Policy;
import org.uriplay.media.entity.Version;
import org.uriplay.media.entity.simple.BrandSummary;
import org.uriplay.media.entity.simple.Item;
import org.uriplay.media.entity.simple.UriplayQueryResult;
import org.uriplay.media.util.ChildFinder;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * {@link BeanGraphWriter} that translates the full URIplay object model
 * into a simplified form and renders that as XML.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class FullToSimpleModelTranslator implements BeanGraphWriter {

	private final BeanGraphWriter outputWriter;

	public FullToSimpleModelTranslator(BeanGraphWriter xmlOutputter) {
		this.outputWriter = xmlOutputter;
	}

	private Iterable<Object> rootsOf(Collection<Object> beans) {
		return Iterables.filter(beans, Predicates.not(new ChildFinder(beans)));
	}
	
	@Override
	public void writeTo(Collection<Object> fullGraph, OutputStream stream) {
		
		UriplayQueryResult outputGraph = new UriplayQueryResult();
		Set<Object> processed = Sets.newHashSet();
		
		Iterable<Object> beansToProcess = rootsOf(fullGraph);
		
		for (Object bean : beansToProcess) {
			
			if (bean instanceof org.uriplay.media.entity.Playlist && !processed.contains(bean)) {
				
				org.uriplay.media.entity.Playlist playList = (org.uriplay.media.entity.Playlist) bean;
				outputGraph.addPlaylist(simplePlaylistFrom(playList, processed));
				processed.add(playList);
			}
			
		}
	
		for (Object bean : fullGraph) {
			
			if (bean instanceof org.uriplay.media.entity.Item && !processed.contains(bean)) {
				
				outputGraph.addItem(simpleItemFrom((org.uriplay.media.entity.Item) bean));
				
			}
			
		}
		
		outputWriter.writeTo(Sets.newHashSet((Object) outputGraph), stream);
	}

	static org.uriplay.media.entity.simple.Playlist simplePlaylistFrom(Playlist fullPlayList, Set<Object> processed) {

		org.uriplay.media.entity.simple.Playlist simplePlaylist = new org.uriplay.media.entity.simple.Playlist();
		
		copyBasicPlaylistAttributes(fullPlayList, simplePlaylist);
		
		for (Playlist fullSubList : fullPlayList.getPlaylists()) {
			simplePlaylist.addPlaylist(simplePlaylistFrom(fullSubList, processed));
			processed.add(fullSubList);
		}
		
		for (org.uriplay.media.entity.Item fullItem : fullPlayList.getItems()) {
			simplePlaylist.addItem(simpleItemFrom(fullItem));
			processed.add(fullItem);
		}
		
		return simplePlaylist;
	}

	private static void copyBasicPlaylistAttributes(Playlist fullPlayList, org.uriplay.media.entity.simple.Playlist simplePlaylist) {
		simplePlaylist.setUri(fullPlayList.getCanonicalUri());
		simplePlaylist.setAliases(fullPlayList.getAliases());
		simplePlaylist.setCurie(fullPlayList.getCurie());
		simplePlaylist.setTitle(fullPlayList.getTitle());
		simplePlaylist.setPublisher(fullPlayList.getPublisher());
		simplePlaylist.setDescription(fullPlayList.getDescription());
		
		simplePlaylist.setImage(fullPlayList.getImage());
		simplePlaylist.setThumbnail(fullPlayList.getThumbnail());
		
		simplePlaylist.setContainedIn(fullPlayList.getContainedInUris());
		simplePlaylist.setGenres(fullPlayList.getGenres());
		simplePlaylist.setTags(fullPlayList.getTags());
	}

	static org.uriplay.media.entity.simple.Item simpleItemFrom(org.uriplay.media.entity.Item fullItem) {
		
		org.uriplay.media.entity.simple.Item simpleItem = new org.uriplay.media.entity.simple.Item();
		
		for (Version version : fullItem.getVersions()) {
			addTo(simpleItem, version);
		}
		
		copyProperties(fullItem, simpleItem);
		
		return simpleItem;
	}

	private static void addTo(Item simpleItem, Version version) {
		
		for (Encoding encoding : version.getManifestedAs()) {
			addTo(simpleItem, version, encoding);
		}
		
		for (Broadcast broadcast : version.getBroadcasts()) {
			org.uriplay.media.entity.simple.Broadcast simpleBroadcast = simplify(broadcast);
			copyProperties(version, simpleBroadcast);
			simpleItem.addBroadcast(simpleBroadcast);
		}
	}

	private static org.uriplay.media.entity.simple.Broadcast simplify(Broadcast broadcast) {
		return new org.uriplay.media.entity.simple.Broadcast(broadcast.getBroadcastOn(), broadcast.getTransmissionTime(), broadcast.getTransmissionEndTime());
	}

	private static void addTo(Item simpleItem, Version version, Encoding encoding) {
		for (Location location : encoding.getAvailableAt()) {
			addTo(simpleItem, version, encoding, location);
		}
	}

	private static void addTo(Item simpleItem, Version version, Encoding encoding, Location location) {
		
		org.uriplay.media.entity.simple.Location simpleLocation = new org.uriplay.media.entity.simple.Location();
		
		copyProperties(version, simpleLocation);
		copyProperties(encoding, simpleLocation);
		copyProperties(location, simpleLocation);
		
		simpleItem.addLocation(simpleLocation);
	}

	private static void copyProperties(org.uriplay.media.entity.Item fullItem, Item simpleItem) {
		
		simpleItem.setUri(fullItem.getCanonicalUri());
		simpleItem.setAliases(fullItem.getAliases());
		simpleItem.setCurie(fullItem.getCurie());
		
		Set<String> containedInUris = fullItem.getContainedInUris();
		for (String uri : containedInUris) {
			simpleItem.addContainedIn(uri);
		}
		
		if (fullItem instanceof Episode) {
			Episode episode = (Episode) fullItem;
			
			simpleItem.setEpisodeNumber(episode.getEpisodeNumber());
			simpleItem.setSeriesNumber(episode.getSeriesNumber());
			
			if (episode.getBrand() != null) {
				Brand brand = episode.getBrand();
				BrandSummary brandSummary = new BrandSummary();

				brandSummary.setUri(brand.getCanonicalUri());
				brandSummary.setCurie(brand.getCurie());
				brandSummary.setTitle(brand.getTitle());
				brandSummary.setDescription(brand.getDescription());
				
				simpleItem.setBrandSummary(brandSummary);
			}
		}
		
		simpleItem.setTitle(fullItem.getTitle());
		simpleItem.setDescription(fullItem.getDescription());
		simpleItem.setPublisher(fullItem.getPublisher());
		simpleItem.setImage(fullItem.getImage());
		simpleItem.setThumbnail(fullItem.getThumbnail());
		simpleItem.setGenres(fullItem.getGenres());
		simpleItem.setTags(fullItem.getTags());
		
		
	}

	private static void copyProperties(Version version, org.uriplay.media.entity.simple.Version simpleLocation) {

		simpleLocation.setPublishedDuration(version.getPublishedDuration());
		simpleLocation.setDuration(version.getDuration());
		simpleLocation.setRating(version.getRating());
		simpleLocation.setRatingText(version.getRatingText());
	}

	private static void copyProperties(Encoding encoding, org.uriplay.media.entity.simple.Location simpleLocation) {

		simpleLocation.setAdvertisingDuration(encoding.getAdvertisingDuration());
		simpleLocation.setAudioBitRate(encoding.getAudioBitRate());
		simpleLocation.setAudioChannels(encoding.getAudioChannels());
		simpleLocation.setBitRate(encoding.getBitRate());
		simpleLocation.setContainsAdvertising(encoding.getContainsAdvertising());
		if (encoding.getDataContainerFormat() != null) {
			simpleLocation.setDataContainerFormat(encoding.getDataContainerFormat().toString());
		}
		simpleLocation.setDataSize(encoding.getDataSize());
		simpleLocation.setDistributor(encoding.getDistributor());
		simpleLocation.setHasDOG(encoding.getHasDOG());
		simpleLocation.setSource(encoding.getSource());
		simpleLocation.setVideoAspectRatio(encoding.getVideoAspectRatio());
		simpleLocation.setVideoBitRate(encoding.getVideoBitRate());
		
		if (encoding.getVideoCoding() != null) {
			simpleLocation.setVideoCoding(encoding.getVideoCoding().toString());
		}
		
		simpleLocation.setVideoFrameRate(encoding.getVideoFrameRate());
		simpleLocation.setVideoHorizontalSize(encoding.getVideoHorizontalSize());
		simpleLocation.setVideoProgressiveScan(encoding.getVideoProgressiveScan());
		simpleLocation.setVideoVerticalSize(encoding.getVideoVerticalSize());
	}

	private static void copyProperties(Location location, org.uriplay.media.entity.simple.Location simpleLocation) {
		Policy policy = location.getPolicy();
		if (policy != null) {
			if (policy.getAvailabilityStart() != null) {
				simpleLocation.setAvailabilityStart(policy.getAvailabilityStart().toDate());
			}
			if (policy.getDrmPlayableFrom() != null) {
				simpleLocation.setDrmPlayableFrom(policy.getDrmPlayableFrom().toDate());
			}
			if (policy.getAvailableCountries() != null) {
				simpleLocation.setAvailableCountries(Countries.toCodes(policy.getAvailableCountries()));
			}
		}
		
		simpleLocation.setTransportIsLive(location.getTransportIsLive());
	    if (location.getTransportType() != null) {
	    	simpleLocation.setTransportType(location.getTransportType().toString());
	    }
	    if (location.getTransportSubType() != null) {
	    	simpleLocation.setTransportSubType(location.getTransportSubType().toString());
	    }
	    simpleLocation.setUri(location.getUri());
	    simpleLocation.setEmbedCode(location.getEmbedCode());
	    simpleLocation.setAvailable(location.getAvailable());
	    
	}

}
