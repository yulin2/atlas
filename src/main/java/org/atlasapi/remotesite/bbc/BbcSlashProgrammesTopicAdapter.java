package org.atlasapi.remotesite.bbc;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.regex.Pattern;

import org.atlasapi.media.entity.TopicRef;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.SiteSpecificAdapter;

import com.metabroadcast.common.base.Maybe;

public class BbcSlashProgrammesTopicAdapter implements SiteSpecificAdapter<TopicRef> {

    private static final String slashProgrammeTopicUriPrefix = "http://www.bbc.co.uk/programmes/topics/";
    private static final Pattern slashProgrammesTopicUriPattern = Pattern.compile(Pattern.quote(slashProgrammeTopicUriPrefix) + "[\\d\\w_]+");
    
    private final RemoteSiteClient<SlashProgrammesRdf> topicRdfClient;
    private final ContentExtractor<SlashProgrammesRdf, Maybe<TopicRef>> topicExtractor;
    
    public BbcSlashProgrammesTopicAdapter(RemoteSiteClient<SlashProgrammesRdf> topicRdfClient, ContentExtractor<SlashProgrammesRdf, Maybe<TopicRef>> topicExtractor) {
        this.topicRdfClient = topicRdfClient;
        this.topicExtractor = topicExtractor;
    }
    
    @Override
    public TopicRef fetch(String uri) {
        checkArgument(canFetch(uri));
        try {
            SlashProgrammesRdf slashProgrammesTopic = topicRdfClient.get(uri + ".rdf");
            Maybe<TopicRef> extract = topicExtractor.extract(slashProgrammesTopic);
            return extract.valueOrNull();
        } catch (Exception e) {
            throw new FetchException("Exception fetching topic at " + uri, e);
        }
    }

    @Override
    public boolean canFetch(String uri) {
        return slashProgrammesTopicUriPattern.matcher(uri).matches();
    }

}
