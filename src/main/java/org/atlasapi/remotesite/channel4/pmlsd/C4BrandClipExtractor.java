package org.atlasapi.remotesite.channel4.pmlsd;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Policy.Platform;
import org.joda.time.DateTime;

import com.google.common.base.Optional;
import com.metabroadcast.common.time.Clock;
import com.sun.syndication.feed.atom.Entry;

public class C4BrandClipExtractor extends C4MediaItemExtractor<Clip> {

    private final C4AtomEntryVersionExtractor versionExtractor;

    public C4BrandClipExtractor(Clock clock) {
        super(clock);
        // TODO: Do we have platform-specific clips?
        versionExtractor = new C4AtomEntryVersionExtractor(Optional.<Platform>absent());
    }

    @Override
    protected Clip createItem(Entry entry, Map<String, String> lookup) {
        return C4PmlsdModule.contentFactory().createClip();
    }
    
    @Override
    protected String getUri(Entry entry, Map<String, String> lookup) {
        return C4AtomApi.clipUri(entry);
    }
    
    @Override
    protected Clip setAdditionalItemFields(Entry entry, Map<String, String> lookup, Clip clip) {
        String fourOdUri = C4AtomApi.fourOdUri(entry);
        if (fourOdUri != null) {
            clip.addAliasUrl(fourOdUri);
        }
        clip.setIsLongForm(false);
        clip.addVersion(versionExtractor.extract(data(entry, fourOdUri, lookup, clip.getLastUpdated())));
        return clip;
    }

    private C4VersionData data(Entry entry, String fourOdUri, Map<String, String> lookup, DateTime lastUpdated) {
        String uri = fourOdUri != null ? fourOdUri : C4AtomApi.clipUri(entry);
        checkNotNull(uri, "No version URI extracted for %s", entry.getId());
        return new C4VersionData(entry.getId(), uri, getMedia(entry), lookup, lastUpdated);
    }
    
}
