package org.atlasapi.remotesite.bbc;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Set;

import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesEpisode;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesTopic;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class BbcSlashProgrammesTopicsAdapter implements SiteSpecificAdapter<List<TopicRef>> {

    private final RemoteSiteClient<SlashProgrammesRdf> rdfClient;
    private final SiteSpecificAdapter<TopicRef> singleTopicFetcher;

    public BbcSlashProgrammesTopicsAdapter(RemoteSiteClient<SlashProgrammesRdf> rdfClient, SiteSpecificAdapter<TopicRef> singleTopicFetcher) {
        this.rdfClient = rdfClient;
        this.singleTopicFetcher = singleTopicFetcher;
    }
    
    @Override
    public List<TopicRef> fetch(String uri) {
        checkArgument(canFetch(uri));
        return ImmutableList.copyOf(Iterables.filter(Iterables.transform(topicsFor(uri), new Function<SlashProgrammesTopic, TopicRef>() {
            @Override
            public TopicRef apply(SlashProgrammesTopic input) {
                String topicUri = topicUriFrom(input);
                return singleTopicFetcher.canFetch(topicUri) ? singleTopicFetcher.fetch(topicUri) : null;
            }

        }), Predicates.notNull()));
    }

    private Iterable<SlashProgrammesTopic> topicsFor(String uri) {
        try {
            return topicsFrom(rdfClient.get(uri+".rdf"));
        } catch (Exception e) {
            return ImmutableList.of();
        }
    }

    private String topicUriFrom(SlashProgrammesTopic topic) {
        return "http://www.bbc.co.uk" + topic.resourceUri().substring(0, topic.resourceUri().indexOf("#"));
    }

    private Iterable<SlashProgrammesTopic> topicsFrom(SlashProgrammesRdf content) {
        return Iterables.concat(subjectsFrom(content), peopleFrom(content), placesFrom(content));
    }

    private Set<SlashProgrammesTopic> subjectsFrom(SlashProgrammesRdf content) {
        SlashProgrammesEpisode episode = content.episode();
        return episode != null && episode.subjects() != null ? episode.subjects() : ImmutableSet.<SlashProgrammesTopic>of();
    }
    
    private Set<SlashProgrammesTopic> placesFrom(SlashProgrammesRdf content) {
        SlashProgrammesEpisode episode = content.episode();
        return episode != null && episode.subjects() != null ? episode.places() : ImmutableSet.<SlashProgrammesTopic>of();
    }
    
    private Set<SlashProgrammesTopic> peopleFrom(SlashProgrammesRdf content) {
        SlashProgrammesEpisode episode = content.episode();
        return episode != null && episode.subjects() != null ? episode.people() : ImmutableSet.<SlashProgrammesTopic>of();
    }

    @Override
    public boolean canFetch(String uri) {
        return BbcFeeds.isACanonicalSlashProgrammesUri(uri);
    }

}
