package org.atlasapi.remotesite.bbc;

import static org.atlasapi.remotesite.bbc.BbcFeeds.isACanonicalSlashProgrammesUri;

import java.util.List;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.RelatedLink;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.SlashProgrammesContainer.SlashProgrammesRelatedLink;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class BbcRelatedLinksAdapter implements SiteSpecificAdapter<List<RelatedLink>> {

    private final RemoteSiteClient<SlashProgrammesContainer> programmesClient;
    
    private static final Pattern TWITTER_URI_PATTERN = Pattern.compile("https?://(www.)?twitter.com(/.*)?");
    private static final Pattern FACEBOOK_URI_PATTERN = Pattern.compile("https?://(www.)?facebook.com(/.*)?");

    public BbcRelatedLinksAdapter(RemoteSiteClient<SlashProgrammesContainer> slashProgrammesClient) {
        this.programmesClient = slashProgrammesClient;
    }
    
    @Override
    public List<RelatedLink> fetch(String uri) {
        Preconditions.checkArgument(canFetch(uri), "Invalid uri: " + uri);
        
        SlashProgrammesContainer slashProgrammesContainer;
        try {
            slashProgrammesContainer = programmesClient.get(addJsonSuffix(uri));
        } catch (Exception e) {
            return ImmutableList.of();
        }

        return ImmutableList.copyOf(Iterables.transform(slashProgrammesContainer.getProgramme().getLinks(),
            new Function<SlashProgrammesRelatedLink, RelatedLink>() {
                @Override
                public RelatedLink apply(SlashProgrammesRelatedLink input) {
                	String url = input.getUrl();
                	if(TWITTER_URI_PATTERN.matcher(url).matches()) {
                		return RelatedLink.twitterLink(url).withTitle(input.getTitle()).build();
                	}
                	else if(FACEBOOK_URI_PATTERN.matcher(url).matches()) {
                		return RelatedLink.facebookLink(url).withTitle(input.getTitle()).build();
                	}
                	else return RelatedLink.unknownTypeLink(url).withTitle(input.getTitle()).build();
                }
            }
        ));

    }

    private String addJsonSuffix(String uri) {
        return uri + ".json";
    }

    @Override
    public boolean canFetch(String uri) {
        return isACanonicalSlashProgrammesUri(uri);
    }

}
