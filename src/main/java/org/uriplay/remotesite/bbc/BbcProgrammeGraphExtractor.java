/* Copyright 2010 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.uriplay.remotesite.bbc;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGenerator;
import org.jherd.beans.id.IdGeneratorFactory;
import org.jherd.util.Maybe;
import org.springframework.beans.MutablePropertyValues;
import org.uriplay.media.TransportType;
import org.uriplay.media.entity.Broadcast;
import org.uriplay.media.entity.Encoding;
import org.uriplay.media.entity.Episode;
import org.uriplay.media.entity.Location;
import org.uriplay.media.entity.Version;
import org.uriplay.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesContainerRef;
import org.uriplay.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesEpisode;
import org.uriplay.remotesite.bbc.SlashProgrammesVersionRdf.BbcBroadcast;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

public class BbcProgrammeGraphExtractor implements BeanGraphExtractor<BbcProgrammeSource> {

	static final String BBC_PUBLISHER = "bbc.co.uk";
	
	private final IdGeneratorFactory idGeneratorFactory;
	private final BbcSeriesNumberResolver seriesResolver;
	
	public BbcProgrammeGraphExtractor(IdGeneratorFactory idGeneratorFactory, BbcSeriesNumberResolver seriesResolver) {
		this.idGeneratorFactory = idGeneratorFactory;
		this.seriesResolver = seriesResolver;
	}

	public Representation extractFrom(BbcProgrammeSource source) {
		Representation representation = new Representation();

		IdGenerator idGenerator = idGeneratorFactory.create();

		String versionId = idGenerator.getNextId();
		
		String webEncodingId = idGenerator.getNextId();
		String webLocationId = idGenerator.getNextId();
		
		String episodeUri = source.getUri();
		
		SlashProgrammesRdf episode = source.episode();
		SlashProgrammesContainerRef container = episode.brand();
		
		if (container == null) {
			container = episode.series();
		}
		
		addEpisodePropertiesTo(representation, episodeUri, versionId, container, episode.episode(), source.getSlashProgrammesUri(), seriesNumber(episode));

		addVersionPropertiesTo(representation, versionId, webEncodingId, source.version(), idGenerator);
		
		addWebPageEncodingLocationTo(representation, episodeUri, source.isAvailable(), webEncodingId, webLocationId);
		
		return representation;
	}
	
	private Maybe<Integer> seriesNumber(SlashProgrammesRdf episode) {
		if (episode.series() != null && episode.series().uri() != null) {
			return seriesResolver.seriesNumberFor(episode.series().uri());
		}
		return Maybe.nothing();
	}

	private void addWebPageEncodingLocationTo(Representation representation, String episodeUri, boolean available, String webEncodingId, String webLocationId) {

		representation.addType(webEncodingId, Encoding.class);
		representation.addAnonymous(webEncodingId);
		
		representation.addType(webLocationId, Location.class);
		representation.addAnonymous(webLocationId);
		
		MutablePropertyValues empvs = new MutablePropertyValues();
		empvs.addPropertyValue("availableAt", Sets.newHashSet(webLocationId));
		representation.addValues(webEncodingId, empvs);
		
		MutablePropertyValues lmpvs = new MutablePropertyValues();
		lmpvs.addPropertyValue("uri", iplayerPageFrom(episodeUri));
		lmpvs.addPropertyValue("transportType", TransportType.HTMLEMBED);
		lmpvs.addPropertyValue("available", available);
		representation.addValues(webLocationId, lmpvs);
	}

	private void addVersionPropertiesTo(Representation representation, String versionId, String webEncodingId, SlashProgrammesVersionRdf slashProgrammesVersion, IdGenerator idGenerator) {

		representation.addType(versionId, Version.class);
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("manifestedAs", Sets.newHashSet(webEncodingId));
		
		if (slashProgrammesVersion != null) {
			mpvs.addPropertyValue("transmissionTime", slashProgrammesVersion.lastTransmitted());
			
			if (slashProgrammesVersion.firstBroadcastSlots() != null || slashProgrammesVersion.repeatBroadcastSlots() != null) {
				mpvs.addPropertyValue("broadcasts", broadcastsFrom(slashProgrammesVersion, representation, idGenerator));
			}
			
		}
		
		representation.addValues(versionId, mpvs);
		representation.addAnonymous(versionId);
	}

	@VisibleForTesting
	static Set<String> broadcastsFrom(SlashProgrammesVersionRdf slashProgrammesVersion, Representation representation, IdGenerator idGenerator) {

		Set<String> broadcastIds = Sets.newHashSet();
		
		Set<BbcBroadcast> broadcasts = Sets.newHashSet();

		if (slashProgrammesVersion.firstBroadcastSlots() != null) {
			broadcasts .addAll(slashProgrammesVersion.firstBroadcastSlots());
		}

		if (slashProgrammesVersion.repeatBroadcastSlots() != null) {
			broadcasts.addAll(slashProgrammesVersion.repeatBroadcastSlots());
		}

		for (BbcBroadcast bbcBroadcast : broadcasts) {
			
			String broadcastId = idGenerator.getNextId();
			broadcastIds.add(broadcastId);
			
			representation.addType(broadcastId, Broadcast.class);
			representation.addAnonymous(broadcastId);
			
			MutablePropertyValues mpvs = new MutablePropertyValues();
			
			mpvs.addPropertyValue("transmissionTime", bbcBroadcast.broadcastDateTime());
			mpvs.addPropertyValue("broadcastOn", channelUrlFrom(bbcBroadcast.broadcastOn()));
			mpvs.addPropertyValue("broadcastDuration", bbcBroadcast.broadcastDuration());
			mpvs.addPropertyValue("scheduleDate", bbcBroadcast.scheduleDate());
			representation.addValues(broadcastId, mpvs);
		}
		
		return broadcastIds;
	}

	private static String channelUrlFrom(String broadcastOn) {
		if (broadcastOn.contains("#")) {
			broadcastOn = broadcastOn.substring(0, broadcastOn.indexOf('#'));
		}
		return "http://www.bbc.co.uk" + broadcastOn;
	}

	private void addEpisodePropertiesTo(Representation representation, String episodeUri, String versionId, SlashProgrammesContainerRef container, SlashProgrammesEpisode slashProgrammesEpisode, String slashProgrammesUri, Maybe<Integer> seriesNumber) {
		representation.addType(episodeUri, Episode.class);
		representation.addUri(episodeUri);
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("title", episodeTitle(slashProgrammesEpisode, seriesNumber));
		if (slashProgrammesEpisode != null) {
			mpvs.addPropertyValue("description", slashProgrammesEpisode.description());
			mpvs.addPropertyValue("episodeNumber", slashProgrammesEpisode.episodeNumber());
			mpvs.addPropertyValue("genres", new BbcProgrammesGenreMap().map(slashProgrammesEpisode.genreUris()));
		}
		
		if (seriesNumber.hasValue()) {
			mpvs.addPropertyValue("seriesNumber", seriesNumber.requireValue());
		}
		
		Set<String> aliases = bbcAliasUrisFor(episodeUri);
		if (!aliases.isEmpty()) {
			mpvs.addPropertyValue("aliases", aliases);
		}
		
		mpvs.addPropertyValue("curie", BbcUriCanonicaliser.curieFor(episodeUri));
		mpvs.addPropertyValue("isLongForm", true);
		
		mpvs.addPropertyValue("publisher", BBC_PUBLISHER);
		mpvs.addPropertyValue("versions", Sets.newHashSet(versionId));
		if (container != null) {
			mpvs.addPropertyValue("containedIn", Sets.newHashSet(container.uri()));
		}
		mpvs.addPropertyValue("thumbnail", thumbnailUrlFrom(episodeUri));
		mpvs.addPropertyValue("image", imageUrlFrom(episodeUri));
		
		representation.addValues(episodeUri, mpvs);
	}

	private static final Pattern titleIsEpisodeAndNumber = Pattern.compile("^Episode \\d+$");
	
	private String episodeTitle(SlashProgrammesEpisode slashProgrammesEpisode, Maybe<Integer> seriesNumber) {
		String title = slashProgrammesEpisode.title();
		if (seriesNumber.isNothing() || !titleIsEpisodeAndNumber.matcher(title).matches()) {
			return title;
		}
		return "Series " +  seriesNumber.requireValue() + " " + title;
	}

	static Set<String> bbcAliasUrisFor(String episodeUri) {
		String pid = BbcUriCanonicaliser.bbcProgrammeIdFrom(episodeUri);
		HashSet<String> aliases = Sets.newHashSet();
		if (pid != null) {
			aliases.add(String.format("http://www.bbc.co.uk/iplayer/episode/b00%s", pid));
			aliases.add(String.format("http://www.bbc.co.uk/programmes/b00%s", pid));
			aliases.add(String.format("http://bbc.co.uk/i/%s/", pid));
			aliases.remove(episodeUri);
		}
		return aliases;
	}
	
	private String imageUrlFrom(String episodeUri) {
		return extractImageUrl(episodeUri, "_640_360.jpg");
	}
	
	private String thumbnailUrlFrom(String episodeUri) {
		return extractImageUrl(episodeUri,  "_150_84.jpg");
	}
	
	private final Pattern programmeIdPattern = Pattern.compile("(b00[a-z0-9]+).*");
	
	private String programmeIdFrom(String uri) {
		Matcher matcher = programmeIdPattern.matcher(uri);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
	
	private String extractImageUrl(String episodeUri, String suffix) {
		return "http://www.bbc.co.uk/iplayer/images/episode/" + programmeIdFrom(episodeUri) + suffix;
	}

	private String iplayerPageFrom(String episodeUri) {
		return "http://www.bbc.co.uk/iplayer/episode/" + programmeIdFrom(episodeUri);
	}

	@SuppressWarnings(value="unused")
	private String embedCode(String locationUri, String epImageUrl) {
		return "<embed width=\"640\" height=\"395\""
				+ "flashvars=\"embedReferer=http://www.bbc.co.uk/iplayer/&amp;domId=bip-play-emp&amp;config=http://www.bbc.co.uk/emp/iplayer/config.xml&amp;playlist="+ locationUri + "&amp;holdingImage=" + epImageUrl + "&amp;config_settings_bitrateFloor=0&amp;config_settings_bitrateCeiling=2500&amp;config_settings_transportHeight=35&amp;config_settings_showPopoutCta=false&amp;config_messages_diagnosticsMessageBody=Insufficient bandwidth to stream this programme. Try downloading instead, or see our diagnostics page.&amp;config_settings_language=en&amp;guidance=unknown\""
				+ "allowscriptaccess=\"always\"" + "allowfullscreen=\"true\" wmode=\"default\" "
				+ "quality=\"high\"" + "bgcolor=\"#000000\"" + "name=\"bbc_emp_embed_bip-play-emp\" "
				+ "id=\"bbc_emp_embed_bip-play-emp\" style=\"\" "
				+ "src=\"http://www.bbc.co.uk/emp/9player.swf?revision=10344_10753\" "
				+ "type=\"application/x-shockwave-flash\"/>";
	}
}
