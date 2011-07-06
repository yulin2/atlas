package org.atlasapi.remotesite.bbc.ion;

import static org.atlasapi.media.entity.Publisher.BBC;
import static org.atlasapi.persistence.logging.AdapterLogEntry.warnEntry;
import static org.atlasapi.persistence.logging.AdapterLogEntry.Severity.WARN;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.atlasapi.media.entity.Actor;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.CrewMember;
import org.atlasapi.media.entity.CrewMember.Role;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.MediaType;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.bbc.BbcFeeds;
import org.atlasapi.remotesite.bbc.BbcProgrammeEncodingAndLocationCreator;
import org.atlasapi.remotesite.bbc.BbcProgrammeGraphExtractor;
import org.atlasapi.remotesite.bbc.ion.BbcIonDeserializers.BbcIonDeserializer;
import org.atlasapi.remotesite.bbc.ion.model.IonContributor;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisodeDetail;
import org.atlasapi.remotesite.bbc.ion.model.IonEpisodeDetailFeed;
import org.atlasapi.remotesite.bbc.ion.model.IonOndemandChange;
import org.atlasapi.remotesite.bbc.ion.model.IonVersion;
import org.joda.time.Duration;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.primitives.Ints;
import com.google.gson.reflect.TypeToken;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpClientBuilder;
import com.metabroadcast.common.http.SimpleHttpRequest;
import com.metabroadcast.common.time.SystemClock;

public class BbcIonEpisodeDetailItemFetcherClient implements BbcItemFetcherClient {

    private static final String CURIE_BASE = "bbc:";
    private static final String EPISODE_DETAIL_PATTERN = "http://www.bbc.co.uk/iplayer/ion/episodedetail/episode/%s/include_broadcasts/1/clips/include/next_broadcasts/1/allow_unavailable/1/format/json";
    private static final String ACTOR_ROLE_NAME = "ACTOR";
    private static final String PERSON_BASE_URL = "http://www.bbc.co.uk/people/";
    private static final String PERSON_BASE_CURIE = "bbc:person_";

    private final BbcIonDeserializer<IonEpisodeDetailFeed> ionDeserialiser = BbcIonDeserializers.deserializerForType(new TypeToken<IonEpisodeDetailFeed>(){});
    private final BbcProgrammeEncodingAndLocationCreator encodingCreator = new BbcProgrammeEncodingAndLocationCreator(new SystemClock());
    private final SimpleHttpClient httpClient = new SimpleHttpClientBuilder()
        .withUserAgent(HttpClients.ATLAS_USER_AGENT)
        .withSocketTimeout(30, TimeUnit.SECONDS)
        .build();
    
    private final AdapterLog log;
    
    private final HttpResponseTransformer<IonEpisodeDetailFeed> TRANSFORMER = new HttpResponseTransformer<IonEpisodeDetailFeed>() {

        @Override
        public IonEpisodeDetailFeed transform(HttpResponsePrologue response, InputStream in) throws HttpException, IOException {
            return ionDeserialiser.deserialise(new InputStreamReader(in, response.getCharsetOrDefault(Charsets.UTF_8)));
        }
    };
    
    public BbcIonEpisodeDetailItemFetcherClient(AdapterLog log) {
        this.log = log;
    }
    
    private IonEpisodeDetail getEpisodeDetail(String pid) {
        try {
            return httpClient.get(new SimpleHttpRequest<IonEpisodeDetailFeed>(String.format(EPISODE_DETAIL_PATTERN, pid), TRANSFORMER)).getBlocklist().get(0);
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(getClass()).withDescription("Couldn't get episode detail for " + pid));
            return null;
        }
    }
    
    @Override
    public Item createItem(String episodeId) {
        IonEpisodeDetail episodeDetail = getEpisodeDetail(episodeId);
        if(episodeDetail != null) {
            return createItemFrom(episodeDetail);
        }
        return null;
    }

    private Item createItemFrom(IonEpisodeDetail episodeDetail) {
        Item item = null;
        if (!Strings.isNullOrEmpty(episodeDetail.getBrandId()) || !Strings.isNullOrEmpty(episodeDetail.getSeriesId())) {
            item = new Episode(BbcFeeds.slashProgrammesUriForPid(episodeDetail.getId()), CURIE_BASE+episodeDetail.getId(), BBC);
            updateEpisodeDetails((Episode)item, episodeDetail);
        } else {
            item = new Item(BbcFeeds.slashProgrammesUriForPid(episodeDetail.getId()), CURIE_BASE+episodeDetail.getId(), BBC);
        }
        return updateItemDetails(item, episodeDetail);
    }

    private void updateEpisodeDetails(Episode item, IonEpisodeDetail episodeDetail) {
        if(!Strings.isNullOrEmpty(episodeDetail.getSeriesId())) {
            item.setSeriesRef(new ParentRef(BbcFeeds.slashProgrammesUriForPid(episodeDetail.getSeriesId())));
        }
        if(Strings.isNullOrEmpty(episodeDetail.getSubseriesId()) && episodeDetail.getPosition() != null) {
            item.setEpisodeNumber(Ints.saturatedCast(episodeDetail.getPosition()));
        }
    }

    private Item updateItemDetails(Item item, IonEpisodeDetail episode) {
        
        item.setTitle(getTitle(episode));
        item.setDescription(episode.getSynopsis());
        
        if (!Strings.isNullOrEmpty(episode.getId())) {
            addImagesTo(episode.getMyImageBaseUrl().toString(), episode.getId(),item);
        }

        if(episode.getVersions() != null) {
            for (IonVersion ionVersion : episode.getVersions()) {
                item.addVersion(versionFrom(ionVersion, episode.getId()));
            }
        }
        
        if (episode.getContributors() != null) {
            for (IonContributor contributor: episode.getContributors()) {
                Maybe<CrewMember> possiblePerson = personFrom(contributor);
                if (possiblePerson.hasValue()) {
                	item.addPerson(possiblePerson.requireValue());
                } else {
                	log.record(new AdapterLogEntry(WARN).withSource(getClass()).withDescription("Unknown person: " + contributor.getRoleName()));
                }
            }
        }

        String masterbrand = episode.getMasterbrand();
        if(!Strings.isNullOrEmpty(masterbrand)) {
            Maybe<MediaType> maybeMediaType = BbcIonMediaTypeMapping.mediaTypeForService(masterbrand);
            if(maybeMediaType.hasValue()) {
                item.setMediaType(maybeMediaType.requireValue());
            } else {
                log.record(warnEntry().withSource(getClass()).withDescription("No mediaType mapping for " + masterbrand));
            }
            
            Maybe<Specialization> maybeSpecialisation = BbcIonMediaTypeMapping.specialisationForService(masterbrand);
            if(maybeSpecialisation.hasValue()) {
                item.setSpecialization(maybeSpecialisation.requireValue());
            } else {
                log.record(warnEntry().withSource(getClass()).withDescription("No specialisation mapping for " + masterbrand));
            }
        }
        return item;
    }

    private String getTitle(IonEpisodeDetail episode) {
        String title = !Strings.isNullOrEmpty(episode.getOriginalTitle()) ? episode.getOriginalTitle() : episode.getTitle();
        if(!Strings.isNullOrEmpty(episode.getSubseriesTitle())) {
            title = String.format("%s %s", episode.getSubseriesTitle(), title);
        }
        return title;
    }

	static void addImagesTo(String prefix, String pid, Content item) {
		String path = prefix + pid;
		item.setThumbnail(path + "_150_84.jpg");
		item.setImage(path + "_640_360.jpg");
	}
    
    private Maybe<CrewMember> personFrom(IonContributor contributor) {
        CrewMember person = null;
        String uri = PERSON_BASE_URL+contributor.getId();
        String curie = PERSON_BASE_CURIE+contributor.getId();
        
        if (ACTOR_ROLE_NAME.equals(contributor.getRoleName())) {
            person = new Actor(uri, curie, Publisher.BBC).withCharacter(contributor.getCharacterName()).withRole(Role.ACTOR);
        } else {
            Maybe<Role> role = Role.fromPossibleKey(contributor.getRole().toLowerCase().replace(' ', '_'));
            if (role.isNothing()) {
            	return Maybe.nothing();
            }
            person = new CrewMember(uri, curie, Publisher.BBC).withRole(role.requireValue());
        }
        person.withName(contributor.getGivenName()+" "+contributor.getFamilyName()).withProfileLink("http://www.bbc.co.uk"+contributor.getSearchUrl());
        person.setLastUpdated(contributor.getUpdated());
        
        return Maybe.just(person);
    }

//    private Broadcast broadcastFrom(IonBroadcast ionBroadcast) {
//        String serviceUri = BbcIonServices.get(ionBroadcast.getService());
//        if(serviceUri == null) {
//            log.record(new AdapterLogEntry(WARN).withDescription("Couldn't find service URI for Ion Service " + ionBroadcast.getService()).withSource(getClass()));
//            return null;
//        } else {
//            Broadcast broadcast = new Broadcast(serviceUri, ionBroadcast.getStart(), ionBroadcast.getEnd());
//            broadcast.withId(CURIE_BASE + ionBroadcast.getId()).setScheduleDate(ionBroadcast.getDate().toLocalDate());
//            broadcast.setLastUpdated(ionBroadcast.getUpdated());
//            return broadcast;
//        }
//    }
    
    private Version versionFrom(IonVersion ionVersion, String pid) {
        Version version = new Version();
        version.setCanonicalUri(BbcFeeds.slashProgrammesUriForPid(ionVersion.getId()));
        BbcProgrammeGraphExtractor.setDurations(version, ionVersion);
        version.setProvider(BBC);
        if(ionVersion.getDuration() != null) {
            version.setDuration(Duration.standardSeconds(ionVersion.getDuration()));
        }
//        if(ionVersion.getBroadcasts() != null) {
//            for (IonBroadcast ionBroadcast : ionVersion.getBroadcasts()) {
//                Broadcast broadcast = broadcastFrom(ionBroadcast);
//                if(broadcast != null) {
//                    version.addBroadcast(broadcast);
//                }
//            }
//        }
        if(ionVersion.getOndemands() != null) {
            for (IonOndemandChange ondemand : ionVersion.getOndemands()) {
                Maybe<Encoding> possibleEncoding = encodingCreator.createEncoding(ondemand, pid);
                if(possibleEncoding.hasValue()) {
                    version.addManifestedAs(possibleEncoding.requireValue());
                }
            }
        }
        return version;
    }
}
