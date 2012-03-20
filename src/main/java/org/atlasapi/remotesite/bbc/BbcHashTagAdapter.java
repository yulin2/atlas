package org.atlasapi.remotesite.bbc;

import static org.atlasapi.remotesite.bbc.BbcFeeds.isACanonicalSlashProgrammesUri;

import java.util.List;

import org.atlasapi.media.content.KeyPhrase;
import org.atlasapi.media.content.Publisher;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.html.HtmlNavigator;
import org.jdom.Element;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class BbcHashTagAdapter implements SiteSpecificAdapter<List<KeyPhrase>> {

    private static final double DEFAULT_WEIGHTING = 1.0;
    private static final String BUZZ_SUFFIX = "/buzz";
    private final RemoteSiteClient<HtmlNavigator> buzzPageClient;
    
    public BbcHashTagAdapter(RemoteSiteClient<HtmlNavigator> buzzPageClient) {
        this.buzzPageClient = buzzPageClient;
    }

    @Override
    public List<KeyPhrase> fetch(String uri) {
        Preconditions.checkArgument(canFetch(uri), "Invalid uri: " + uri);

        List<Element> hashTagElements = ImmutableList.of();
        
        try {
            HtmlNavigator buzzPageNavigator = buzzPageClient.get(buzzUriFor(uri));
            hashTagElements = buzzPageNavigator.allElementsMatching("//ul[@id='hashtag-list']/li/a");
        } catch (Exception e) {
            return ImmutableList.of();
        }

        return ImmutableList.copyOf(Iterables.transform(hashTagElements, new Function<Element, KeyPhrase>() {
            @Override
            public KeyPhrase apply(Element input) {
                return new KeyPhrase(input.getTextTrim(), Publisher.BBC, DEFAULT_WEIGHTING);
            }
        }));
    }
    
    private String buzzUriFor(String uri) {
        if(!uri.endsWith(BUZZ_SUFFIX)) {
            uri = uri + BUZZ_SUFFIX;
        }
        return uri;
    }

    @Override
    public boolean canFetch(String uri) {
        if(uri.endsWith(BUZZ_SUFFIX)) {
            uri = uri.substring(0, uri.lastIndexOf(BUZZ_SUFFIX));
        }
        return isACanonicalSlashProgrammesUri(uri);
    }
}
