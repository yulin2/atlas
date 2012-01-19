package org.atlasapi.remotesite.bbc;

import static org.atlasapi.remotesite.bbc.BbcFeeds.isACanonicalSlashProgrammesUri;

import java.util.List;

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
                    return RelatedLink.unknownTypeLink(input.getUrl()).withTitle(input.getTitle()).build();
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
