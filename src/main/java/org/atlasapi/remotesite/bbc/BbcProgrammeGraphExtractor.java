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

package org.atlasapi.remotesite.bbc;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Broadcast;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesContainerRef;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesEpisode;
import org.atlasapi.remotesite.bbc.SlashProgrammesVersionRdf.BbcBroadcast;
import org.joda.time.LocalDate;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;

public class BbcProgrammeGraphExtractor implements ContentExtractor<BbcProgrammeSource, Item> {

	static final String FULL_IMAGE_EXTENSION = "_640_360.jpg";
	static final String THUMBNAIL_EXTENSION = "_150_84.jpg";

	private final BbcSeriesNumberResolver seriesResolver;
	private final BbcProgrammesPolicyClient policyClient;
	
	public BbcProgrammeGraphExtractor(BbcSeriesNumberResolver seriesResolver, BbcProgrammesPolicyClient policyClient) {
		this.seriesResolver = seriesResolver;
		this.policyClient = policyClient;
	}

	public BbcProgrammeGraphExtractor() {
		this(new SeriesFetchingBbcSeriesNumberResolver(), new BbcProgrammesPolicyClient());
	}

	public Item extract(BbcProgrammeSource source) {
		
		String episodeUri = source.getUri();
		
		SlashProgrammesRdf episode = source.episode();
		SlashProgrammesContainerRef container = episode.brand();
		
		if (container == null) {
			container = episode.series();
		}
		
		Location location = htmlLinkLocation(episodeUri);
		
		Encoding encoding = new Encoding();
		encoding.addAvailableAt(location);
		
		Version version = version(source.version());
		version.addManifestedAs(encoding);
		
		Item item = item(episodeUri, container, episode, source.getSlashProgrammesUri());
		item.addVersion(version);
		
		return item;
	}
	
	private Maybe<Integer> seriesNumber(SlashProgrammesRdf episode) {
		if (episode.series() != null && episode.series().uri() != null) {
			return seriesResolver.seriesNumberFor(episode.series().uri());
		}
		return Maybe.nothing();
	}

	private Location htmlLinkLocation(String episodeUri) {
		Location location = new Location();
		location.setUri(iplayerPageFrom(episodeUri));
		location.setTransportType(TransportType.LINK);
		
		Maybe<Policy> policy = policyClient.policyForUri(episodeUri);
		
		if (policy.hasValue()) {
			location.setPolicy(policy.requireValue());
		}
		
		location.setAvailable(policy.hasValue());
		return location;
	}

	private Version version(SlashProgrammesVersionRdf slashProgrammesVersion) {
		Version version = new Version();
		if (slashProgrammesVersion != null) {
			if (slashProgrammesVersion.broadcastSlots() != null) {
				version.setBroadcasts(broadcastsFrom(slashProgrammesVersion));
			}
		}
		return version;
	}

	@VisibleForTesting
	static Set<Broadcast> broadcastsFrom(SlashProgrammesVersionRdf slashProgrammesVersion) {

		Set<Broadcast> broadcasts = Sets.newHashSet();
		
		Set<BbcBroadcast> bbcBroadcasts = Sets.newHashSet();

		if (slashProgrammesVersion.broadcastSlots() != null) {
			bbcBroadcasts.addAll(slashProgrammesVersion.broadcastSlots());
		}

		for (BbcBroadcast bbcBroadcast : bbcBroadcasts) {
			
			Broadcast broadcast = new Broadcast(channelUrlFrom(bbcBroadcast.broadcastOn()), bbcBroadcast.broadcastDateTime(), bbcBroadcast.broadcastEndDateTime());
			
			if (bbcBroadcast.scheduleDate != null) {
				broadcast.setScheduleDate(new LocalDate(bbcBroadcast.scheduleDate()));
			}
			broadcasts.add(broadcast);
		}
		
		return broadcasts;
	}

	private static String channelUrlFrom(String broadcastOn) {
		if (broadcastOn.contains("#")) {
			broadcastOn = broadcastOn.substring(0, broadcastOn.indexOf('#'));
		}
		return "http://www.bbc.co.uk" + broadcastOn;
	}

	private Item item(String episodeUri, SlashProgrammesContainerRef container, SlashProgrammesRdf episode, String slashProgrammesUri) {
		String curie = BbcUriCanonicaliser.curieFor(episodeUri);
		
		Item item = episode.brand() == null ? new Item(episodeUri, curie, Publisher.BBC) : new Episode(episodeUri, curie, Publisher.BBC);
		
		Maybe<Integer> seriesNumber = seriesNumber(episode);
		
		SlashProgrammesEpisode slashProgrammesEpisode = episode.episode();
		item.setTitle(episodeTitle(slashProgrammesEpisode, seriesNumber));
		
		if (slashProgrammesEpisode != null) {
			item.setDescription(slashProgrammesEpisode.description());
			item.setGenres(new BbcProgrammesGenreMap().map(slashProgrammesEpisode.genreUris()));
		}
		
		if (item instanceof Episode) {
			if (seriesNumber.hasValue()) {
				((Episode) item).setSeriesNumber(seriesNumber.requireValue());
			}
			Integer episodeNumber = slashProgrammesEpisode.episodeNumber();
			if (episodeNumber != null) {
				((Episode) item).setEpisodeNumber(episodeNumber);
			}
		}
		
		Set<String> aliases = bbcAliasUrisFor(episodeUri);
		if (!aliases.isEmpty()) {
			item.setAliases(aliases);
		}
		
		item.setIsLongForm(true);
		
		item.setThumbnail(thumbnailUrlFrom(episodeUri));
		item.setImage(imageUrlFrom(episodeUri));
		
		return item;
	}

	private static final Pattern titleIsEpisodeAndNumber = Pattern.compile("^Episode \\d+$");
	
	private String episodeTitle(SlashProgrammesEpisode slashProgrammesEpisode, Maybe<Integer> seriesNumber) {
		String title = slashProgrammesEpisode.title();
		if (seriesNumber.isNothing() || ! titleIsEpisodeAndNumber.matcher(title).matches()) {
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
		return extractImageUrl(episodeUri, FULL_IMAGE_EXTENSION);
	}
	
	private String thumbnailUrlFrom(String episodeUri) {
		return extractImageUrl(episodeUri,  THUMBNAIL_EXTENSION);
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
