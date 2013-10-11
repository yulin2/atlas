package org.atlasapi.remotesite.channel4.pmlsd;

import java.util.Map;

import org.atlasapi.media.entity.Clip;
import org.atlasapi.media.entity.Policy.Platform;
import org.jdom.Element;

import com.google.common.base.Optional;
import com.metabroadcast.common.time.Clock;
import com.sun.syndication.feed.atom.Entry;

public class C4BrandClipExtractor extends BaseC4ItemExtractor<Clip> {

    private final C4AtomEntryVersionExtractor versionExtractor;

    public C4BrandClipExtractor(Clock clock) {
        super(clock);
        // TODO: Do we have platform-specific clips?
        versionExtractor = new C4AtomEntryVersionExtractor(Optional.<Platform>absent(), clock);
    }

    @Override
    protected Clip createItem(Entry entry, Map<String, String> lookup) {
        return new Clip();
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
        clip.addVersion(versionExtractor.extract(entry));
        return clip;
    }
    
    @Override
    protected Element getMedia(Entry source) {
        return C4AtomApi.mediaGroup(source);
    }

}
